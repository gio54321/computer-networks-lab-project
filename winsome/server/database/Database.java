package winsome.server.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import winsome.common.responses.CommentResponse;
import winsome.common.responses.PartialRewardResponse;
import winsome.common.responses.PostResponse;
import winsome.common.responses.UserResponse;
import winsome.lib.utils.Wrapper;
import winsome.server.database.exceptions.AuthenticationException;
import winsome.server.database.exceptions.UserAlreadyExistsException;
import winsome.server.database.exceptions.UserAlreadyLoggedInException;
import winsome.server.database.exceptions.UserDoesNotExistsException;
import winsome.server.database.serializables.SerializableDatabase;

/**
 * Main database class of Winsome server.
 * This provides the main functionalities of the application.
 * 
 * There is an internal R/W lock.
 * Every operaration must acquire the operation lock my the methods obeginOp
 * and endOp
 * For the operations that must have exclusive access to the entire database,
 * like reward calculation and database dumping for persistence,
 * the user must acquire the exclusive access by the methods beginExclusive and
 * endExclusive
 */
public class Database {
    // the reward percentage of the author
    private final double authorCut;

    // the authentication provider
    private AuthenticationProvider authProvider = new AuthenticationProvider();
    // the users map (username -> user)
    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    // the authTokens map (username -> authToken)
    private ConcurrentHashMap<String, String> authTokens = new ConcurrentHashMap<>();
    // the posts map (postId -> post)
    private ConcurrentHashMap<Integer, Post> posts = new ConcurrentHashMap<>();
    // the id provider
    private IdProvider idProvider = new IdProvider();

    // read write lock used to get exclusive access to the entire structure
    // by the reward calculator and the persistence manager
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Lock opLock = rwLock.readLock();
    private Lock exclusiveLock = rwLock.writeLock();

    /**
     * Constructor for Database
     * 
     * @param authorCut how much the author gets in rewards compared to the
     *                  curators. Must be a number between 0 and 1.
     */
    public Database(double authorCut) {
        this.authorCut = authorCut;
    }

    /**
     * Begin a non exclusive access operation
     */
    public void beginOp() {
        this.opLock.lock();
    }

    /**
     * End a non exclusive access operation
     */
    public void endOp() {
        this.opLock.unlock();
    }

    /**
     * Begin an exclusive access operation
     */
    public void beginExclusive() {
        this.exclusiveLock.lock();
    }

    /**
     * End an exclusive access operation
     */
    public void endExclusive() {
        this.exclusiveLock.unlock();
    }

    /**
     * clone databse to a serializable database.
     * This method must be called with exclusive access to the database
     * 
     * @return the clonedserializable database
     */
    public SerializableDatabase cloneToSerializable() {
        var res = new SerializableDatabase();
        // clone the id provider state
        res.initPostId = this.idProvider.getCurrentState();

        // clone the posts
        res.posts = new HashMap<>();
        for (var p : this.posts.keySet()) {
            res.posts.put(p, this.posts.get(p).cloneToSerializable());
        }

        // clone the users
        res.users = new HashMap<>();
        for (var u : this.users.keySet()) {
            res.users.put(u, this.users.get(u).cloneToSerializable());
        }

        return res;
    }

    /**
     * Import the database from a serializable database
     * All the field in this object will be set to a copy of
     * the input argument
     * This method must be called with exclusive access to the database
     * 
     * @param database
     */
    public void fromSerializable(SerializableDatabase database) {
        // set the id provider state
        this.idProvider.setCurrentState(database.initPostId);

        // clone the users
        this.users.clear();
        for (var username : database.users.keySet()) {
            var newUser = new User();
            newUser.fromSerializable(database.users.get(username));
            this.users.put(username, newUser);
        }

        // clone the posts
        this.posts.clear();
        for (var postId : database.posts.keySet()) {
            var newPost = new Post();
            newPost.fromSerializable(database.posts.get(postId));
            this.posts.put(postId, newPost);
        }
    }

    /**
     * Register a new user to Winsome
     * 
     * @param user the user to be registered
     * @throws UserAlreadyExistsException if there exists enother user with the same
     *                                    username
     */
    public void registerUser(User user) throws UserAlreadyExistsException {
        Wrapper<Boolean> userAlreadyExists = new Wrapper<>(false);
        // try to put the new user
        this.users.compute(user.getUsername(), (k, v) -> {
            if (v == null) {
                // user is not already in database
                // so put the new user in the database
                return user;
            } else {
                // there exists already a user with the same username
                // set the flag and do not modify the existing user
                userAlreadyExists.setValue(true);
                return v;
            }
        });

        // if the flag is set throw an error
        if (userAlreadyExists.getValue()) {
            throw new UserAlreadyExistsException();
        }
    }

    /**
     * Login a user to winsome
     * 
     * @param username the user's username
     * @param password the user's password
     * @return the auth token
     * @throws UserDoesNotExistsException   if the user does not exists
     * @throws UserAlreadyLoggedInException if the user is already logged in
     * @throws AuthenticationException      if the credentials are invalid
     */
    public String login(String username, String password)
            throws UserDoesNotExistsException, UserAlreadyLoggedInException, AuthenticationException {
        // get the user entry in the user map
        // thread safe since users cannot be deleted
        var user = this.users.get(username);

        // if the user does not exists, throw an error
        if (user == null) {
            throw new UserDoesNotExistsException();
        }

        // if the passwords do not match, throw an error
        if (!user.getPassword().contentEquals(password)) {
            throw new AuthenticationException();
        }

        // generate new tokens until there are no more collisions
        // (it is very very unlikely that there will be a collision but
        // not checking for it would be incorrect)
        var newAuthToken = this.authProvider.generateNewToken();
        while (this.authTokens.values().contains(newAuthToken)) {
            newAuthToken = this.authProvider.generateNewToken();
        }

        // this final var is introduced because in lambdas all captured variables
        // must be final or effectively final
        final var finalNewAuthtoken = newAuthToken;

        // try to insert the new authToken in the authTokens map
        Wrapper<Boolean> userAlreadyLoggedIn = new Wrapper<>(false);
        this.authTokens.compute(username, (k, v) -> {
            if (v == null) {
                // the user was not logged in
                // return the newly generated authToken
                return finalNewAuthtoken;
            } else {
                // the user was already logged in
                // set the flag to true
                userAlreadyLoggedIn.setValue(true);
                return v;
            }
        });

        // if the user was already logged in, throw an error
        if (userAlreadyLoggedIn.getValue()) {
            throw new UserAlreadyLoggedInException();
        }

        return newAuthToken;
    }

    /**
     * Logout the user from Winsome
     * 
     * @param username the username to log out
     */
    public void logout(String username) {
        this.authTokens.remove(username);
    }

    /**
     * Get an user response from username
     * The user response can be serialized
     * 
     * @param username the required user's username
     * @return null if the user does not exists, otherwise the forged UserResponse
     */
    public UserResponse getUserResponse(String username) {
        var user = this.users.get(username);
        if (user == null) {
            return null;
        }
        return new UserResponse(username, user.getTags());
    }

    /**
     * Check if the authentication credentials are valid
     * 
     * @param username  the user's username
     * @param authToken the user's authToken
     * @return true if the pair username/authToken is valid
     */
    public boolean authenticateUser(String username, String authToken) {
        Wrapper<Boolean> validAuth = new Wrapper<>(true);
        this.authTokens.compute(username, (k, v) -> {
            if (v == null || !v.contentEquals(authToken)) {
                // the auth is valid if the auth token exists and
                // matches the input one
                validAuth.setValue(false);
            }
            return v;
        });
        return validAuth.getValue();
    }

    /**
     * List the users in the database that have at least one tag in common with
     * the given user. Returns a list of UserResponse
     * 
     * @param username the given user, must be a valid username
     * @return the list of UserResponse, null if the username is invalid
     * @throws UserDoesNotExistsException
     */
    public List<UserResponse> listUsers(String username) {
        var list = new ArrayList<UserResponse>();

        // get the calling user
        var callingUser = this.users.get(username);
        if (callingUser == null) {
            return null;
        }

        // compute the list of users that have one tag in common
        // with the calling user
        this.users.forEach((k, v) -> {
            if (!k.contentEquals(username) && callingUser.hasTagInCommon(v)) {
                list.add(new UserResponse(k, v.getTags()));
            }
        });
        return list;
    }

    /**
     * Follow user
     * Add a relation in the database that username follows toFollowUsername
     * 
     * @param username     the calling username, must be a valid username
     * @param toFollowUser the user to follow
     * @return true if the user was not already following toFollowUsername (so if
     *         the operation modified the database), false otherwise (so if the
     *         operation did not modify the database)
     * @throws UserDoesNotExistsException if toFollowUser is not a valid username
     */
    public boolean followUser(String username, String toFollowUser) throws UserDoesNotExistsException {
        // check if toFollowUser exists in the database
        if (!this.users.containsKey(toFollowUser)) {
            throw new UserDoesNotExistsException();
        }

        System.out.println("follow " + username + "->" + toFollowUser);

        // add the new relation to the calling user's followed map
        Wrapper<Boolean> notAlreadyFollowing = new Wrapper<Boolean>(true);
        this.users.compute(username, (k, v) -> {
            if (v.getFollowed().contains(toFollowUser)) {
                // if the relation was already present, reset the flag
                notAlreadyFollowing.setValue(false);
            }
            v.addFollowed(toFollowUser);
            return v;
        });
        // add the new relation to the followed user's followers map
        this.users.compute(toFollowUser, (k, v) -> {
            v.addFollower(username);
            return v;
        });
        return notAlreadyFollowing.getValue();
    }

    /**
     * Unfollow user
     * Remove the relation in the database of username following toUnfollowUser
     * 
     * @param username      the calling username, must be a valid username
     * @param toUnollowUser the user to unfollow
     * @return true if the user was following toFollowUsername (so if
     *         the operation modified the database), false otherwise (so if the
     *         operation did not modify the database)
     * @throws UserDoesNotExistsException if toUnfollowUser is not valid
     */
    public boolean unfollowUser(String username, String toUnfollowUser) throws UserDoesNotExistsException {
        // check if toUnfollowUser exists in the database
        if (!this.users.containsKey(toUnfollowUser)) {
            throw new UserDoesNotExistsException();
        }

        System.out.println("unfollow " + username + "->" + toUnfollowUser);
        Wrapper<Boolean> alreadyFollowing = new Wrapper<Boolean>(false);
        // remove the relation in the calling user's map
        this.users.compute(username, (k, v) -> {
            if (v.getFollowed().contains(toUnfollowUser)) {
                // if the relation was already present, set the flag
                alreadyFollowing.setValue(true);
            }
            v.removeFollowed(toUnfollowUser);
            return v;
        });
        // remove the relation in toUnfollowUser's map
        this.users.compute(toUnfollowUser, (k, v) -> {
            v.removeFollower(username);
            return v;
        });
        return alreadyFollowing.getValue();
    }

    /**
     * List followers of a given user. Return a list of UserResponse
     * 
     * @param username the calling user, must be a valid username
     * @return the list of followers
     */
    public List<UserResponse> listFollowers(String username) {
        // get the followers list
        var usernamesList = new ArrayList<String>();
        this.users.compute(username, (k, v) -> {
            usernamesList.addAll(v.getFollowers());
            return v;
        });

        // get the tags of the followers
        var userResponseList = new ArrayList<UserResponse>();
        for (var u : usernamesList) {
            userResponseList.add(new UserResponse(u, this.users.get(u).getTags()));
        }
        return userResponseList;
    }

    /**
     * List the users that a given user follows.
     * Returns a list of UserResponse
     * 
     * @param username the calling user, must be a valid username
     * @return the list of followings
     */
    public List<UserResponse> listFollowing(String username) {
        // get the list of following usernames
        var usernamesList = new ArrayList<String>();
        this.users.compute(username, (k, v) -> {
            usernamesList.addAll(v.getFollowed());
            return v;
        });

        // get the tags of the followed
        var userResponseList = new ArrayList<UserResponse>();
        for (var u : usernamesList) {
            userResponseList.add(new UserResponse(u, this.users.get(u).getTags()));
        }
        return userResponseList;
    }

    /**
     * Add a post to the database. Returns the new post's id
     * 
     * @param author  the author's username, must be a valid username
     * @param title   the post title
     * @param content the post content
     * @return the newly created post id
     */
    public int addPostToDatabase(String author, String title, String content) {
        if (author == null || title == null || content == null) {
            throw new NullPointerException();
        }

        // get a new id
        var newId = this.idProvider.getNewId();
        // forge the post
        var post = new Post(newId, author, title, content);

        // put the post in the posts map
        this.posts.put(newId, post);

        // add the post id to the authored posts set of the author
        this.users.compute(author, (k, v) -> {
            v.addAuthoredPost(newId);
            return v;
        });
        return newId;
    }

    /**
     * Check if a postId exists
     * 
     * @param postId
     * @return true if there exists a post with id postId
     */
    public boolean postExists(int postId) {
        return this.posts.containsKey(postId);
    }

    /**
     * Copy a post into a postResponse
     * 
     * @param post
     * @param postResponse
     */
    private void copyPostIntoPostResponse(Post post, PostResponse postResponse) {
        // copy all the fields
        postResponse.author = post.getAuthorUsername();
        postResponse.title = post.getTitle();
        postResponse.postId = post.getPostId();
        postResponse.content = post.getContent();
        postResponse.positiveVoteCount = post.getPositiveVotesCount();
        postResponse.negativeVoteCount = post.getNegativeVotesCount();

        // copy comments
        var comments = post.getComments();
        var commentResponses = new CommentResponse[comments.size()];
        var i = 0;
        for (var c : comments) {
            commentResponses[i] = new CommentResponse(c.getAuthor(), c.getContent());
            ++i;
        }
        postResponse.comments = commentResponses;
    }

    /**
     * Get the post's author from postId
     * 
     * @param postId
     * @return null if postId is not a valid id, the author's username otherwise
     */
    public String getPostAuthor(int postId) {
        // try to get the author of the post
        Wrapper<String> out = new Wrapper<String>("");
        this.posts.computeIfPresent(postId, (k, v) -> {
            out.setValue(v.getAuthorUsername());
            return v;
        });

        // if the post is not present in the database return null
        if (out.getValue().contentEquals("")) {
            return null;
        } else {
            return out.getValue();
        }
    }

    /**
     * Get the post from postId
     * 
     * @param postId
     * @return null if postId is not a valid id, the mapped post otherwise
     */
    public PostResponse getPostFromId(int postId) {
        PostResponse outPost = new PostResponse();
        Wrapper<Boolean> postExists = new Wrapper<>(false);

        // since posts can be deleted, the entire operation must be atomic
        // on the posts map
        this.posts.compute(postId, (k, v) -> {
            if (v != null) {
                postExists.setValue(true);
                copyPostIntoPostResponse(v, outPost);
            }
            return v;
        });

        if (postExists.getValue()) {
            return outPost;
        } else {
            return null;
        }
    }

    /**
     * Rewin a post
     * 
     * @param callingUsername the calling username, must be a valid username
     * @param postId          the post id to rewin, must be a valid post id
     * @return true if the rewin was successful, false otherwise
     */
    public boolean rewinPost(String callingUsername, int postId) {
        Wrapper<Boolean> wasNotRewinned = new Wrapper<>(false);
        this.users.compute(callingUsername, (k, v) -> {
            if (v != null) {
                // try to rewin the post
                var rewinRes = v.addRewinnedPost(postId);
                // set the flag value to store if the post was actually rewinned
                wasNotRewinned.setValue(rewinRes);
            }
            return v;
        });
        return wasNotRewinned.getValue();
    }

    /**
     * Rate a post
     * 
     * @param callingUsername the athor of the rate, must be a valid username
     * @param postId          the post id to rate, must be a valid post id
     * @param rate            the rate
     * @return true if the post was actually rated, false otherwise
     */
    public boolean ratePost(String callingUsername, int postId, int rate) {
        Wrapper<Boolean> wasNotAlreadyRated = new Wrapper<>(true);
        this.posts.compute(postId, (k, v) -> {
            if (v != null) {
                // check if the post was already rated by the calling user
                if (v.hasUserRated(callingUsername)) {
                    wasNotAlreadyRated.setValue(false);
                } else {
                    v.addRate(callingUsername, rate);
                }
            }
            return v;
        });
        return wasNotAlreadyRated.getValue();
    }

    /**
     * Get the posts in give user's blog, including rewins
     * 
     * @param username the user to get the blog, must be a valid username
     * @return the list of post ids in the user's blog
     */
    public List<Integer> getPostsIdsFromAuthor(String username) {
        // a tree set is used to create the feed set
        // since the result will we ordered (and therefore ordered by time)
        var postList = new TreeSet<Integer>();
        this.users.compute(username, (k, v) -> {
            if (v != null) {
                // add to the feed the authored posts of the following user
                postList.addAll(v.getAuthoredPosts());
                // add to the feed the posts that the following user rewinned
                postList.addAll(v.getRewins());
            }
            return v;
        });

        var resultingList = new ArrayList<Integer>();
        // the iterator is guaranteed to be in ascending order
        for (var id : postList) {
            resultingList.add(id);
        }
        return resultingList;
    }

    /**
     * Get the feed of given user. This includes the posts created by
     * the user's followings and the rewins made by the user's following.
     * 
     * @param username the user to compute the feed set, must be a valid username
     * @return
     */
    private SortedSet<Integer> getFeedSet(String username) {
        var following = new HashSet<String>();

        // get the user's following set
        this.users.compute(username, (k, v) -> {
            if (v != null) {
                following.addAll(v.getFollowed());
            }
            return v;
        });

        // construct the union of all the following users blogs
        var postSet = new TreeSet<Integer>();
        for (var user : following) {
            postSet.addAll(getPostsIdsFromAuthor(user));
        }
        return postSet;
    }

    /**
     * Check if the given post is in the feed of given user
     * 
     * @param username the given username, must be a valid username
     * @param postId   the given post to check, must be a valid post id
     * @return true if the post is in the user's feed
     */
    public boolean postIsInFeed(String username, int postId) {
        return getFeedSet(username).contains(postId);
    }

    /**
     * Get the feed post ids of a given user.
     * This is equivalent to getFeedSet but
     * excluding the posts that are authored by the user
     * For example if user A created post 42, user B rewins post 42, A follows B
     * the post 42 will not be in A's feed list.
     * 
     * @param username the user to compute the feed, must be a valid username
     * @return the list of post ids
     */
    public List<Integer> getFeedPostIds(String username) {
        var postSet = getFeedSet(username);
        var resultingPostList = new ArrayList<Integer>();
        // the iterator is guaranteed to be in ascending order
        for (var id : postSet) {
            // filter out the posts that are authored by the calling user
            Wrapper<Boolean> postValid = new Wrapper<>(false);
            this.posts.compute(id, (k, v) -> {
                if (v != null && !v.getAuthorUsername().contentEquals(username)) {
                    postValid.setValue(true);
                }
                return v;
            });

            if (postValid.getValue()) {
                resultingPostList.add(id);
            }
        }
        return resultingPostList;
    }

    /**
     * Given a list of post ids, get a list of post responses
     * 
     * @param postIds a list of post ids
     * @return the list of PostResponse corresponding to the given ids
     */
    public List<PostResponse> getPostReponsesFromIds(List<Integer> postIds) {
        var outList = new ArrayList<PostResponse>();
        for (var id : postIds) {
            // for each post id, costruct the post response and
            // add it to the out list
            PostResponse outPost = new PostResponse();
            Wrapper<Boolean> postExists = new Wrapper<>(false);

            // since posts can be deleted, the entire operation must be atomic
            this.posts.compute(id, (k, v) -> {
                if (v != null) {
                    postExists.setValue(true);
                    copyPostIntoPostResponse(v, outPost);
                }
                return v;
            });

            if (postExists.getValue()) {
                outList.add(outPost);
            }
        }
        return outList;
    }

    /**
     * Add a comment to given post
     * 
     * @param postId         the post to comment, must be a valid post id
     * @param authorUsername the author username
     * @param content        the comment content
     */
    public void addComment(int postId, String authorUsername, String content) {
        this.posts.compute(postId, (k, v) -> {
            if (v != null) {
                v.addComment(authorUsername, content);
            }
            return v;
        });

    }

    /**
     * Delete a post from the database.
     * This has to be done with exclusive access to the database
     * 
     * @param postId the post to be deleted
     */
    public void deletePost(int postId) {
        // deletePost is assumed to be a not frequent operation, so
        // it can take longer than other operations

        // remove the post id from the authors and rewinners
        this.users.forEach((k, v) -> {
            v.removeAuthoredPost(postId);
            v.removeRewinnedPost(postId);
        });

        // remove from post list
        this.posts.remove(postId);
    }

    /**
     * Calculate rewards for posts.
     * This has to be called with exclusive access to the database
     */
    public void calculateRewards() {
        var rewardsMap = new HashMap<String, Double>();

        // for each post compute the rewards
        this.posts.forEach((k, v) -> {
            // get the author
            var author = v.getAuthorUsername();
            // get the curators set
            var curators = v.getCuratorsUsernames();

            // calculate the new reward
            var currentReward = v.calculateNewReward();
            // do something only if currentReward is >=0
            // since a value < 0 means no reward has to be assigned
            if (currentReward >= 0) {
                // add the author's cut to the rewards map
                var authorReward = currentReward * this.authorCut;
                rewardsMap.compute(author, (a, r) -> {
                    if (r == null) {
                        // first iteration of reward
                        return authorReward;
                    } else {
                        return r + authorReward;
                    }
                });

                // split the curators' cut and add the rewards to the reward map
                if (curators.size() > 0) {
                    var curatorsReward = (currentReward * (1 - this.authorCut)) / curators.size();
                    for (var curator : curators) {
                        rewardsMap.compute(curator, (a, r) -> {
                            if (r == null) {
                                // first iteration of reward
                                return curatorsReward;
                            } else {
                                return r + curatorsReward;
                            }
                        });
                    }
                }
            }
        });

        System.out.print(rewardsMap.toString());
        // now that all the rewards have been calculated, add rewards
        // entry to the assigned users
        var now = System.currentTimeMillis();
        for (var user : rewardsMap.keySet()) {
            var partialReward = rewardsMap.get(user);
            this.users.compute(user, (k, v) -> {
                if (v != null) {
                    v.addRewardEntry(now, partialReward);
                }
                return v;
            });
        }
    }

    /**
     * Get reward list of given user.
     * Return a list of partial reward responses
     * 
     * @param username the given user, must be a valid username
     * @return the list of partial reward responses
     */
    public List<PartialRewardResponse> getRewardHistory(String username) {
        var outList = new ArrayList<PartialRewardResponse>();
        this.users.compute(username, (k, v) -> {
            if (v != null) {
                outList.addAll(v.getPartialRewardResponseList());
            }
            return v;
        });
        return outList;
    }

    /**
     * Get the wallet of given user
     * 
     * @param username the given user, must be a valid username
     * @return
     */
    public double getWallet(String username) {
        Wrapper<Double> wallet = new Wrapper<Double>(0.0);
        this.users.compute(username, (k, v) -> {
            if (v != null) {
                wallet.setValue(v.getWallet());
            }
            return v;
        });
        return wallet.getValue();
    }
}

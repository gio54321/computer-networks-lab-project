package winsome.server.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import winsome.common.responses.PostResponse;
import winsome.common.responses.UserResponse;
import winsome.lib.utils.Wrapper;
import winsome.server.database.exceptions.AuthenticationException;
import winsome.server.database.exceptions.UserAlreadyExistsException;
import winsome.server.database.exceptions.UserAlreadyLoggedInException;
import winsome.server.database.exceptions.UserDoesNotExistsException;

public class Database {
    private AuthenticationProvider authProvider = new AuthenticationProvider();
    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> authTokens = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Post> posts = new ConcurrentHashMap<>();
    private IdProvider idProvider = new IdProvider();

    public void registerUser(User user) throws UserAlreadyExistsException {
        Wrapper<Boolean> userAlreadyExists = new Wrapper<>(false);
        this.users.compute(user.getUsername(), (k, v) -> {
            if (v == null) {
                // user not already in db
                return user;
            } else {
                userAlreadyExists.setValue(true);
                return v;
            }
        });

        if (userAlreadyExists.getValue()) {
            throw new UserAlreadyExistsException();
        }
    }

    public String login(String username, String password)
            throws UserDoesNotExistsException, UserAlreadyLoggedInException, AuthenticationException {
        // thread safe because users does not get removed nor modified
        var user = this.users.get(username);
        if (user == null) {
            throw new UserDoesNotExistsException();
        }

        if (!user.getPassword().contentEquals(password)) {
            throw new AuthenticationException();
        }

        Wrapper<Boolean> userAlreadyLoggedIn = new Wrapper<>(false);
        var newAuthToken = this.authProvider.generateNewToken();
        // TODO check for collisions
        this.authTokens.compute(username, (k, v) -> {
            if (v == null) {
                return newAuthToken;
            } else {
                userAlreadyLoggedIn.setValue(true);
                return v;
            }
        });

        if (userAlreadyLoggedIn.getValue()) {
            throw new UserAlreadyLoggedInException();
        }
        return newAuthToken;
    }

    public UserResponse getUserResponse(String username) {
        var user = this.users.get(username);
        if (user == null) {
            return null;
        }
        return new UserResponse(username, user.getTags());
    }

    public void logout(String username) {
        this.authTokens.remove(username);
    }

    public boolean authenticateUser(String username, String authToken) {
        Wrapper<Boolean> validAuth = new Wrapper<>(true);
        this.authTokens.compute(username, (k, v) -> {
            if (v == null || !v.contentEquals(authToken)) {
                validAuth.setValue(false);
            }
            return v;
        });
        return validAuth.getValue();
    }

    // TODO documentation
    public List<UserResponse> listUsers(String username) {
        var list = new ArrayList<UserResponse>();
        var callingUser = this.users.get(username);
        this.users.forEach((k, v) -> {
            if (!k.contentEquals(username) && callingUser.hasTagInCommon(v)) {
                list.add(new UserResponse(k, v.getTags()));
            }
        });
        return list;
    }

    // TODO doc return value true->done, false->noop
    public boolean followUser(String username, String toFollowUser) throws UserDoesNotExistsException {
        if (!this.users.containsKey(toFollowUser)) {
            throw new UserDoesNotExistsException();
        }

        System.out.println("follow " + username + "->" + toFollowUser);
        Wrapper<Boolean> notAlreadyFollowing = new Wrapper<Boolean>(true);

        this.users.compute(username, (k, v) -> {
            if (v.getFollowed().contains(toFollowUser)) {
                notAlreadyFollowing.setValue(false);
            }
            v.addFollowed(toFollowUser);
            return v;
        });
        this.users.compute(toFollowUser, (k, v) -> {
            v.addFollower(username);
            return v;
        });
        return notAlreadyFollowing.getValue();
    }

    // TODO doc return value true->done, false->noop
    public boolean unfollowUser(String username, String toUnfollowUser) throws UserDoesNotExistsException {
        if (!this.users.containsKey(toUnfollowUser)) {
            throw new UserDoesNotExistsException();
        }
        System.out.println("unfollow " + username + "->" + toUnfollowUser);
        Wrapper<Boolean> alreadyFollowing = new Wrapper<Boolean>(false);
        this.users.compute(username, (k, v) -> {
            if (v.getFollowed().contains(toUnfollowUser)) {
                alreadyFollowing.setValue(true);
            }
            v.removeFollowed(toUnfollowUser);
            return v;
        });
        this.users.compute(toUnfollowUser, (k, v) -> {
            v.removeFollower(username);
            return v;
        });
        return alreadyFollowing.getValue();
    }

    public List<UserResponse> listFollowers(String username) {
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

    public List<UserResponse> listFollowing(String username) {
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

    // return the new id
    public int addPostToDatabase(String author, String title, String content) {
        if (author == null || title == null || content == null) {
            throw new NullPointerException();
        }
        var newId = this.idProvider.getNewId();
        var post = new Post(newId, author, title, content);
        this.posts.put(newId, post);
        this.users.compute(author, (k, v) -> {
            v.addAuthoredPost(newId);
            return v;
        });
        return newId;
    }

    public boolean postExists(int postId) {
        return this.posts.containsKey(postId);
    }

    private void copyPostIntoPostResponse(Post post, PostResponse postResponse) {
        postResponse.author = post.getAuthorUsername();
        postResponse.title = post.getTitle();
        postResponse.postId = post.getPostId();
        postResponse.content = post.getContent();
        postResponse.positiveVoteCount = post.getPositiveVotesCount();
        postResponse.negativeVoteCount = post.getNegativeVotesCount();
    }

    public String getPostAuthor(int postId) {
        Wrapper<String> out = new Wrapper<String>("");
        this.posts.computeIfPresent(postId, (k, v) -> {
            out.setValue(v.getAuthorUsername());
            return v;
        });

        if (out.getValue().contentEquals("")) {
            return null;
        } else {
            return out.getValue();
        }
    }

    // return null if the id is not valid
    public PostResponse getPostFromId(int postId) {
        PostResponse outPost = new PostResponse();
        Wrapper<Boolean> postExists = new Wrapper<>(false);

        // since posts can be deleted, the entire operation must be atomic
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

    public boolean rewinPost(String callingUsername, int postId) {
        Wrapper<Boolean> wasNotRewinned = new Wrapper<>(false);
        this.users.compute(callingUsername, (k, v) -> {
            if (v != null) {
                var rewinRes = v.addRewinnedPost(postId);
                wasNotRewinned.setValue(rewinRes);
            }
            return v;
        });
        return wasNotRewinned.getValue();
    }

    public boolean ratePost(String callingUsername, int postId, int rate) {
        Wrapper<Boolean> wasNotAlreadyRated = new Wrapper<>(true);
        this.posts.compute(postId, (k, v) -> {
            if (v != null) {
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

    public List<Integer> getFeedPostIds(String username) {
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

        var resultingPostList = new ArrayList<Integer>();
        // the iterator is guaranteed to be in ascending order
        for (var id : postSet) {
            resultingPostList.add(id);
        }
        return resultingPostList;
    }

    public List<PostResponse> getPostReponsesFromIds(List<Integer> postIds) {
        var outList = new ArrayList<PostResponse>();
        for (var id : postIds) {
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
}

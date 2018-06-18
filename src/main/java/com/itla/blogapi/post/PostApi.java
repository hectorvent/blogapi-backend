/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itla.blogapi.post;

import com.itla.blogapi.user.User;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hectorvent
 */
public class PostApi {

    private final static String PATH = "/post";
    private final static String PATH_ID = PATH + "/:postId";
    private final static String COMMENTS = PATH_ID + "/comment";
    private final PostService postService;
    private final CommentService commentService;

    public PostApi(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    public void start(Router router) throws Exception {

        router.get(PATH).handler(this::getPosts);
        router.get(PATH_ID).handler(this::getPost);
        router.post(PATH).handler(this::addPost);

        // comments
        router.get(COMMENTS).handler(this::getComments);
        router.post(COMMENTS).handler(this::addComment);
    }

    private void getPost(RoutingContext context) {

        //   authenticate(context, r -> {
        String postId = context.request().params().get("postId");

        postService.getPost(Integer.valueOf(postId), res -> {
            if (res.succeeded()) {
                Post post = res.result();
                context.response().setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(post.toJson().encode());
            } else {
                context.response().setStatusCode(404)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", true).put("message", res.cause().getMessage()).toString());
            }
        });
        //    });
    }

    private void getPosts(RoutingContext context) {

        //      authenticate(context, r -> {
        Map<String, String> params = new HashMap<>();

        for (Map.Entry<String, String> param : context.request().params()) {
            params.put(param.getKey(), param.getValue());
        }

        postService.getPosts(params, res -> {
            if (res.succeeded()) {
                JsonArray posts = new JsonArray();
                res.result().stream().map(Post::toJson).forEach(posts::add);
                context.response().setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(posts.encode());
            } else {
                context.response().setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", true).put("message", res.cause().getMessage()).encode());
            }
        });
        //       });

    }

    private void addPost(RoutingContext context) {

//        authenticate(context, r -> {
        Post post = new Post(context.getBodyAsJson());

        User user = context.get("user");
        post.setUserId(user.getId());

        postService.addPost(post, res -> {
            if (res.succeeded()) {

                post.setId(res.result());
                context.response().setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(post.toJson().toString());
            } else {
                context.response().setStatusCode(400)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", true).put("message", res.cause().getMessage()).toString());
            }
        });
        //   });
    }

    private void getComments(RoutingContext context) {

        String sPostId = context.request().params().get("postId");
        Integer postId = Integer.parseInt(sPostId);

        commentService.getComments(postId, res -> {
            if (res.succeeded()) {
                JsonArray posts = new JsonArray();
                res.result().stream().map(Comment::toJson).forEach(posts::add);
                context.response().setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(posts.encode());
            } else {
                context.response().setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", true).put("message", res.cause().getMessage()).encode());
            }
        });
        //     });

    }

    private void addComment(RoutingContext context) {

        Comment comment = new Comment(context.getBodyAsJson());

        String sPostId = context.request().params().get("postId");
        Integer postId = Integer.parseInt(sPostId);
        User user = (User) context.get("user");
        comment.setUserId(user.getId());
        comment.setPostId(postId);

        commentService.addComment(comment, res -> {
            if (res.succeeded()) {

                comment.setId(res.result());
                context.response().setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(comment.toJson().toString());
            } else {
                context.response().setStatusCode(400)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", true).put("message", res.cause().getMessage()).toString());
            }
        });

    }

}

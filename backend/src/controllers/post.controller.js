import mongoose from "mongoose";
import { Post } from "../models/post.model.js";
import { Comment } from "../models/comment.model.js";
import { ApiError } from "../utils/ApiError.js";
import { ApiResponse } from "../utils/ApiResponse.js";

const createPost = async (req, res) => {
    const { content, habitContext } = req.body;

    if (!content || content.trim() === "") {
        throw new ApiError(400, "Post content is required");
    }

    const post = await Post.create({
        content,
        user: req.user._id,
        habitContext: habitContext || null
    });

    return res.status(201).json(
        new ApiResponse(201, post, "Post created successfully")
    );
};

const getFeed = async (req, res) => {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const pipeline = [
        { $sort: { createdAt: -1 } },
        { $skip: skip },
        { $limit: limit },
        {
            $lookup: {
                from: "users",
                localField: "user",
                foreignField: "_id",
                as: "author",
                pipeline: [
                    { $project: { name: 1, _id: 1 } }
                ]
            }
        },
        {
            $lookup: {
                from: "comments",
                localField: "_id",
                foreignField: "post",
                as: "comments"
            }
        },
        {
            $addFields: {
                author: { $first: "$author" },
                likesCount: { $size: "$likes" },
                commentsCount: { $size: "$comments" },
                isLikedByMe: { $in: [req.user._id, "$likes"] }
            }
        },
        {
            $project: {
                content: 1,
                createdAt: 1,
                author: 1,
                likesCount: 1,
                commentsCount: 1,
                isLikedByMe: 1,
                habitContext: 1
            }
        }
    ];

    const posts = await Post.aggregate(pipeline);
    const totalPosts = await Post.countDocuments();

    const metadata = {
        currentPage: page,
        totalPages: Math.ceil(totalPosts / limit),
        totalItems: totalPosts,
        hasNextPage: page * limit < totalPosts
    };

    return res.status(200).json(
        new ApiResponse(200, { posts, metadata }, "Feed retrieved successfully")
    );
};

const toggleReaction = async (req, res) => {
    const { postId } = req.params;
    const userId = req.user._id;

    if (!mongoose.Types.ObjectId.isValid(postId)) {
        throw new ApiError(400, "Invalid Post ID format");
    }

    const post = await Post.findById(postId);

    if (!post) {
        throw new ApiError(404, "Post not found");
    }

    const isLiked = post.likes.includes(userId);

    const updateQuery = isLiked 
        ? { $pull: { likes: userId } } 
        : { $addToSet: { likes: userId } };

    const updatedPost = await Post.findByIdAndUpdate(
        postId,
        updateQuery,
        { new: true }
    );

    return res.status(200).json(
        new ApiResponse(
            200, 
            { isLiked: !isLiked, likesCount: updatedPost.likes.length }, 
            isLiked ? "Post unliked" : "Post liked"
        )
    );
};

const addComment = async (req, res) => {
    const { content } = req.body;
    const { postId } = req.params;

    if (!content || content.trim() === "") {
        throw new ApiError(400, "Comment content is required");
    }

    const comment = await Comment.create({
        content,
        post: postId,
        user: req.user._id
    });

    await comment.populate("user", "_id name"); 

    await Post.findByIdAndUpdate(postId, { $inc: { commentsCount: 1 } });

    return res.status(201).json(
        new ApiResponse(201, comment, "Comment added successfully")
    );
};

const getComments = async (req, res) => {
    const { postId } = req.params;
    
    const comments = await Comment.find({ post: postId })
        .populate("user", "name")
        .sort({ createdAt: -1 });

    return res.status(200).json(
        new ApiResponse(200, comments, "Comments retrieved")
    );
};

export { createPost, getFeed, toggleReaction, addComment, getComments };
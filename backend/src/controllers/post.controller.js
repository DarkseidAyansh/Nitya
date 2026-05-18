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


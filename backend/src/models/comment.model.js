import mongoose, { Schema } from "mongoose";

const commentSchema = new Schema(
    {
        content: {
            type: String,
            required: [true, "Comment content is required"],
            trim: true,
            maxlength: [500, "Comment cannot exceed 500 characters"]
        },
        post: {
            type: Schema.Types.ObjectId,
            ref: "Post",
            required: true,
            index: true
        },
        user: {
            type: Schema.Types.ObjectId,
            ref: "User",
            required: true
        }
    },
    {
        timestamps: true
    }
);

export const Comment = mongoose.model("Comment", commentSchema);
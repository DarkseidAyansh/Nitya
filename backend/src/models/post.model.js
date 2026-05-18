import mongoose, { Schema } from "mongoose";

const postSchema = new Schema(
    {
        content: {
            type: String,
            required: [true, "Post content is required"],
            trim: true,
            maxlength: [2000, "Post cannot exceed 2000 characters"]
        },
        user: {
            type: Schema.Types.ObjectId,
            ref: "User",
            required: true,
            index: true
        },
        habitContext: {
            type: Schema.Types.ObjectId,
            ref: "Habit",
            description: "Optional reference to the habit achieved"
        },
        likes: [
            {
                type: Schema.Types.ObjectId,
                ref: "User"
            }
        ]
    },
    {
        timestamps: true
    }
);


postSchema.index({ createdAt: -1 });

export const Post = mongoose.model("Post", postSchema);
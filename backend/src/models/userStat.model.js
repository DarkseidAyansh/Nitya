import mongoose, { Schema } from "mongoose";

const userStatSchema = new Schema(
    {
        user: {
            type: Schema.Types.ObjectId,
            ref: "User",
            required: true,
            unique: true
        },
        points: {
            type: Number,
            default: 0
        },
        currentStreak: {
            type: Number,
            default: 0
        },
        longestStreak: {
            type: Number,
            default: 0
        },
        lastActivityDate: {
            type: Date,
            default: null
        }
    },
    {
        timestamps: true
    }
);

userStatSchema.index({ points: -1, currentStreak: -1 });

export const UserStat = mongoose.model("UserStat", userStatSchema);
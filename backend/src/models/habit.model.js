import mongoose, { Schema } from "mongoose";

const habitSchema = new Schema(
    {
        user: {
            type: Schema.Types.ObjectId,
            ref: "User",
            required: true,
            index: true
        },
        syncId: {
            type: String,
            required: [true, "Client syncId is required for offline synchronization"],
            index: true
        },
        title: {
            type: String,
            required: [true, "Habit title is required"],
            trim: true
        },
        description: {
            type: String,
            trim: true,
            default: ""
        },
        frequency: {
            type: String,
            enum: ["DAILY", "WEEKLY"],
            default: "DAILY"
        },
        isDeleted: {
            type: Boolean,
            default: false
        },
        clientUpdatedAt: {
            type: Date,
            required: true
        },
        lastCompletedDate: {
            type: String,
            default: null
        },
        streakCount: {
            type: Number,
            default: 0
        }
    },
    {
        timestamps: true
    }
);

habitSchema.index({ user: 1, syncId: 1 }, { unique: true });

export const Habit = mongoose.model("Habit", habitSchema);
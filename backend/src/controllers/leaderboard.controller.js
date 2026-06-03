import { UserStat } from "../models/userStat.model.js";
import { ApiError } from "../utils/ApiError.js";
import { ApiResponse } from "../utils/ApiResponse.js";

const initializeOrGetStats = async (userId) => {
    let stats = await UserStat.findOne({ user: userId });
    if (!stats) {
        stats = await UserStat.create({ user: userId });
    }
    return stats;
};

const getGlobalLeaderboard = async (req, res) => {
    const limit = parseInt(req.query.limit) || 50;

    const pipeline = [
        { $sort: { points: -1, currentStreak: -1 } },
        { $limit: limit },
        {
            $lookup: {
                from: "users",
                localField: "user",
                foreignField: "_id",
                as: "profile"
            }
        },
        {
            $unwind: "$profile"
        },
        {
            $project: {
                _id: 0,
                userId: "$user",
                name: "$profile.name",
                points: 1,
                currentStreak: 1,
                longestStreak: 1
            }
        }
    ];

    const leaderboard = await UserStat.aggregate(pipeline);

    return res.status(200).json(
        new ApiResponse(200, leaderboard, "Global leaderboard retrieved successfully")
    );
};

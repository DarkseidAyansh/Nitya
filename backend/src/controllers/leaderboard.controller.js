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

const getMyRank = async (req, res) => {
    const userId = req.user._id;

    const userStats = await initializeOrGetStats(userId);

    const higherRankedCount = await UserStat.countDocuments({
        $or: [
            { points: { $gt: userStats.points } },
            { 
                points: userStats.points, 
                currentStreak: { $gt: userStats.currentStreak } 
            }
        ]
    });

    const myRank = higherRankedCount + 1;

    const data = {
        rank: myRank,
        points: userStats.points,
        currentStreak: userStats.currentStreak,
        longestStreak: userStats.longestStreak
    };

    return res.status(200).json(
        new ApiResponse(200, data, "User rank retrieved successfully")
    );
};

const awardActivityPoints = async (req, res) => {
    const { pointsToAdd } = req.body;
    const userId = req.user._id;

    if (!pointsToAdd || typeof pointsToAdd !== "number" || pointsToAdd <= 0) {
        throw new ApiError(400, "Valid points amount is required");
    }

    const stats = await initializeOrGetStats(userId);

    const now = new Date();
    const todayNormalized = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    
    let newStreak = stats.currentStreak;
    let newPoints = stats.points + pointsToAdd;

    if (stats.lastActivityDate) {
        const lastActivity = new Date(stats.lastActivityDate);
        const lastActivityNormalized = new Date(lastActivity.getFullYear(), lastActivity.getMonth(), lastActivity.getDate());

        const diffTime = todayNormalized - lastActivityNormalized;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 1) {            newStreak += 1;
        } else if (diffDays === 0) {
            newStreak = stats.currentStreak;
        } else {
            newStreak = 1;
        }
    } else {
        newStreak = 1;
    }

    const newLongestStreak = Math.max(stats.longestStreak, newStreak);

    stats.points = newPoints;
    stats.currentStreak = newStreak;
    stats.longestStreak = newLongestStreak;
    stats.lastActivityDate = now; 

    await stats.save();

    return res.status(200).json(
        new ApiResponse(200, stats, "Activity points and streak updated successfully")
    );
};

export { getGlobalLeaderboard, getMyRank, awardActivityPoints };
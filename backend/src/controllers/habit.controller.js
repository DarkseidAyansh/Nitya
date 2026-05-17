import { Habit } from "../models/habit.model.js";
import { ApiError } from "../utils/ApiError.js";
import { ApiResponse } from "../utils/ApiResponse.js";


const syncHabits = async (req, res) => {
    const { changes, lastSyncAt } = req.body;
    const userId = req.user._id;

    if (changes && Array.isArray(changes) && changes.length > 0) {
        const bulkOperations = changes.map((clientHabit) => {
            return {
                updateOne: {
                    filter: { syncId: clientHabit.syncId, user: userId },
                    update: {
                        $set: {
                            title: clientHabit.title,
                            description: clientHabit.description,
                            frequency: clientHabit.frequency,
                            isDeleted: clientHabit.isDeleted,
                            clientUpdatedAt: clientHabit.clientUpdatedAt,
                            user: userId
                        }
                    },
                    upsert: true
                }
            };
        });

        await Habit.bulkWrite(bulkOperations);
    }

    const query = { user: userId };
    
    if (lastSyncAt) {
        query.updatedAt = { $gt: new Date(lastSyncAt) };
    }

    const serverChanges = await Habit.find(query).select("-__v");

    return res.status(200).json(
        new ApiResponse(
            200,
            {
                serverChanges,
                lastSyncAt: new Date().toISOString()
            },
            "Habits synchronized successfully"
        )
    );
};

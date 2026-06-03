import express from "express";
import cors from "cors";
import cookieParser from "cookie-parser";
import {ApiResponse} from "./utils/ApiResponse.js"
import userRouter from "./routes/user.routes.js";
import { errorHandler } from "./middlewares/error.middleware.js";

import habitRouter from "./routes/habit.routes.js";

import postRouter from "./routes/post.routes.js";

import leaderboardRouter from "./routes/leaderboard.routes.js";


const app = express();

app.use(cors({
    origin: process.env.CORS_ORIGIN,
    credentials: true
}));

app.use(express.json({ limit: "16kb" }));
app.use(express.urlencoded({ extended: true, limit: "16kb" }));
app.use(cookieParser());

app.use("/api/v1/users", userRouter);

app.use("/api/v1/habits", habitRouter);

app.use("/api/v1/posts", postRouter);

app.use("/api/v1/leaderboards", leaderboardRouter);

app.get("/api/v1/health",(req,res)=>{
    return res.status(200).json(
        new ApiResponse(200, null, "Server is healthy and running")
    )
});

app.use(errorHandler);

export { app };
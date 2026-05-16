import dotenv from "dotenv";
import connectDB from "./src/db/db.js";
import { app } from "./src/app.js";

dotenv.config({
    path: "./.env"
});

const PORT = process.env.PORT || 5000;

connectDB()
    .then(() => {
        app.on("error", (error) => {
            console.error("Express App Error: ", error);
            throw error;
        });

        app.listen(PORT, () => {
            console.log(`Server is running on port ${PORT}`);
        });
    })
    .catch((error) => {
        console.error("Database connection failed, terminating server startup.", error);
    });
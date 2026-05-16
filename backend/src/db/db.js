import mongoose from "mongoose";
import dotenv from "dotenv";
import dns from "dns"

dns.setServers([
    '1.1.1.1',
    '8.8.8.8'
])


const connectDB = async () => {
    try {
        const connectionInstance = await mongoose.connect(process.env.MONGO_URI);
        console.log(`\nMongoDB connected.. and  DB Host: ${connectionInstance.connection.host}`);
    } catch (error) {
        console.error("MongoDB connection FAILED: ", error);
        process.exit(1);
    }
};

export default connectDB;
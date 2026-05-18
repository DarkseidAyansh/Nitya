import { Router } from "express";
import { verifyJWT } from "../middlewares/auth.middleware.js";
import { 
    createPost, 
    getFeed, 
    toggleReaction, 
    addComment 
} from "../controllers/post.controller.js";

const router = Router();

// All routes require authentication
router.use(verifyJWT);

router.route("/").post(createPost).get(getFeed);
router.route("/:postId/reaction").post(toggleReaction);
router.route("/:postId/comments").post(addComment);

export default router;
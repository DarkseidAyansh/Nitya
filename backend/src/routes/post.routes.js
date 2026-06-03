import { Router } from "express";
import { verifyJWT } from "../middlewares/auth.middleware.js";
import { 
    createPost, 
    getFeed, 
    toggleReaction, 
    addComment, 
    getComments
} from "../controllers/post.controller.js";

const router = Router();

router.use(verifyJWT);

router.route("/").post(createPost).get(getFeed);
router.route("/:postId/reaction").post(toggleReaction);
router.route("/:postId/comments").post(addComment);
router.route("/:postId/comments").get(getComments);

export default router;
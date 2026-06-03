import { Router } from "express";
import { verifyJWT } from "../middlewares/auth.middleware.js";
import { 
    getGlobalLeaderboard, 
    getMyRank, 
    awardActivityPoints 
} from "../controllers/leaderboard.controller.js";

const router = Router();

router.use(verifyJWT);

router.route("/global").get(getGlobalLeaderboard);
router.route("/my-rank").get(getMyRank);
router.route("/award").post(awardActivityPoints);

export default router;
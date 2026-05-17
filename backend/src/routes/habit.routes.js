import { Router } from "express";
import { verifyJWT } from "../middlewares/auth.middleware.js";
import { syncHabits, getHabits, deleteHabit } from "../controllers/habit.controller.js";

const router = Router();

router.use(verifyJWT);

router.route("/sync").post(syncHabits);
router.route("/").get(getHabits);
router.route("/:syncId").delete(deleteHabit);

export default router;
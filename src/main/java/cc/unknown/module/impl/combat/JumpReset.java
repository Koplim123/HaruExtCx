package cc.unknown.module.impl.combat;

import cc.unknown.event.impl.EventLink;
import cc.unknown.event.impl.move.PreUpdateEvent;
import cc.unknown.event.impl.move.UpdateEvent;
import cc.unknown.event.impl.packet.PacketEvent;
import cc.unknown.event.impl.player.StrafeEvent;
import cc.unknown.module.Module;
import cc.unknown.module.impl.ModuleCategory;
import cc.unknown.module.setting.impl.BooleanValue;
import cc.unknown.module.setting.impl.DescValue;
import cc.unknown.module.setting.impl.ModeValue;
import cc.unknown.module.setting.impl.SliderValue;
import cc.unknown.utils.helpers.MathHelper;
import cc.unknown.utils.player.PlayerUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class JumpReset extends Module {
	private ModeValue mode = new ModeValue("Mode", "Motion", "Normal", "Motion", "Tick", "Hit");
	private SliderValue chance = new SliderValue("Chance", 100, 0, 100, 1);
	private BooleanValue onlyGround = new BooleanValue("Only ground", true);
	private DescValue desc = new DescValue("Options for Motion mode");
	private BooleanValue custom = new BooleanValue("Custom motion", false);
	private BooleanValue aggressive = new BooleanValue("Agressive", false);
	private SliderValue motion = new SliderValue("Motion X/Y", 0.1, 0.0, 0.7, 0.1);

	private int limit = 0;
	private boolean reset = false;

	public JumpReset() {
		super("JumpReset", ModuleCategory.Combat);
		this.registerSetting(mode, chance, onlyGround, desc, custom, aggressive, motion);
	}

	@Override
	public void onEnable() {
		super.onEnable();
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	@EventLink
	public void onUpdate(UpdateEvent e) {
		switch (mode.getMode()) {
		case "Motion":
		case "Hits":
		case "Tick":
		case "Normal":
			if (!(chance.getInput() == 100 || Math.random() <= chance.getInput() / 100))
				return;
			break;
		}
	}

	@EventLink
	public void onPreUpdate(PreUpdateEvent e) {
		if (mc.thePlayer.isInLava() || mc.thePlayer.isBurning() || mc.thePlayer.isInWater() || mc.thePlayer.isInWeb) {
			return;
		}

		switch (mode.getMode()) {
		case "Motion": {
			if (mc.thePlayer.hurtTime > 0 && onlyGround.isToggled() && mc.thePlayer.onGround && mc.thePlayer.fallDistance > 2.5F) {
			    float yaw = e.getYaw() * 0.017453292F;
			    e.setY(0.42D);

			    float sinYaw = MathHelper.sin(yaw);
			    float reduce = (float) (MathHelper.sin(custom.isToggled() ? yaw : (aggressive.isToggled() ? e.getPitch() : yaw)) * motion.getInput());

			    mc.thePlayer.motionX -= sinYaw * reduce;
			    mc.thePlayer.motionY += sinYaw * reduce;
			}
		}
		break;
		}
	}

	@EventLink
	public void onReceive(PacketEvent e) {
		if (e.isReceive()) {
			if (e.getPacket() instanceof S12PacketEntityVelocity) {
				if (((S12PacketEntityVelocity) e.getPacket()).getEntityID() == mc.thePlayer.getEntityId() && PlayerUtil.inGame()) {
					assert mc.thePlayer != null;
					switch (mode.getMode()) {
					case "Hits":
					case "Tick": {
						double motionX = ((S12PacketEntityVelocity) e.getPacket()).motionX;
						double motionZ = ((S12PacketEntityVelocity) e.getPacket()).motionZ;
						double packetDirection = Math.atan2(motionX, motionZ);
						double degreePlayer = PlayerUtil.getDirection();
						double degreePacket = Math.floorMod((int) Math.toDegrees(packetDirection), 360);
						double angle = Math.abs(degreePacket + degreePlayer);
						double threshold = 120.0;
						angle = Math.floorMod((int) angle, 360);
						boolean inRange = angle >= 180 - threshold / 2 && angle <= 180 + threshold / 2;
						if (inRange) {
							reset = true;
						}
					}
					break;
					case "Normal": {
			            if (e.getPacket() instanceof S12PacketEntityVelocity) {
			                if (((S12PacketEntityVelocity) e.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
			                    if(mc.thePlayer.onGround) {
			                        mc.thePlayer.jump();
			                    }
			                }
			            }
					}
					break;
					}
				}
			}
		}
	}

	@EventLink
	public void onStrafe(StrafeEvent event) {
		if (mc.thePlayer == null) {
			return;
		}

		if (mode.is("Ticks") || mode.is("Hits") && reset) {
			if (!mc.gameSettings.keyBindJump.pressed && shouldJump() && mc.thePlayer.isSprinting()
					&& (onlyGround.isToggled() && mc.thePlayer.onGround) && mc.thePlayer.hurtTime == 9
					&& mc.thePlayer.fallDistance > 2.5F) {
				mc.gameSettings.keyBindJump.pressed = true;
				limit = 0;
			}
			reset = false;
			return;
		}

		switch (mode.getMode()) {
		case "Ticks": {
			limit++;
		}
			break;

		case "Hits": {
			if (mc.thePlayer.hurtTime == 9) {
				limit++;
			}
		}
			break;
		}
	}

	private boolean shouldJump() {
		switch (mode.getMode()) {
		case "Ticks": {
			return limit >= MathHelper.randomInt(2, 3);
		}

		case "Hits": {
			return limit >= MathHelper.randomInt(2, 3);
		}
		default:
			return false;
		}
	}
}

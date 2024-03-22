package cc.unknown.module.impl.combat;

import cc.unknown.event.impl.EventLink;
import cc.unknown.event.impl.move.UpdateEvent;
import cc.unknown.module.Module;
import cc.unknown.module.impl.ModuleCategory;
import cc.unknown.module.setting.impl.BooleanValue;
import cc.unknown.module.setting.impl.SliderValue;
import cc.unknown.utils.client.Cold;
import cc.unknown.utils.player.CombatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;

public class AutoRod extends Module {

	private Cold pushTimer = new Cold();
	private Cold rodPullTimer = new Cold();

	private boolean rodInUse = false;
	private int switchBack;

	private BooleanValue facingEnemy = new BooleanValue("Check enemy", true);
	private SliderValue enemyDistance = new SliderValue("Distance enemy", 8, 1, 10, 1);
	private SliderValue pushDelay = new SliderValue("Push delay", 100, 50, 1000, 50);
	private SliderValue pullbackDelay = new SliderValue("Pullback delay", 500, 50, 1000, 50);

	public AutoRod() {
		super("AutoRod", ModuleCategory.Combat);
		this.registerSetting(facingEnemy, enemyDistance, pushDelay, pullbackDelay);
	}

	@EventLink
	public void onUpdate(UpdateEvent e) {

		if ((mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() == Items.fishing_rod) || rodInUse) {
			if (rodPullTimer.elapsed(pullbackDelay.getInputToLong(), true)) {
				if (switchBack != 0 && mc.thePlayer.inventory.currentItem != switchBack) {
					mc.thePlayer.inventory.currentItem = switchBack;
					mc.playerController.updateController();
				} else {
					mc.thePlayer.stopUsingItem();
				}
				switchBack = 0;
				rodInUse = false;
			}
		} else {
			boolean shouldUseRod = facingEnemy.isToggled() ? isFacingEnemy() : true;
			if (shouldUseRod && pushTimer.elapsed(pushDelay.getInputToLong(), true)) {
				int rodSlot = findRod();
				if (rodSlot != -1) {
					mc.thePlayer.inventory.currentItem = rodSlot;
					mc.playerController.updateController();
					rod();
				}
			}
		}
	}

	private boolean isFacingEnemy() {
		Entity facingEntity = mc.objectMouseOver != null ? mc.objectMouseOver.entityHit : null;
		if (facingEntity != null && CombatUtil.instance.canTarget(facingEntity)) {
			return true;
		}

		return false;
	}

	private void rod() {
		int rodSlot = findRod();
		mc.thePlayer.inventory.currentItem = rodSlot;
		mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(rodSlot));
		rodInUse = true;
		rodPullTimer.reset();
	}

	private int findRod() {
		for (int slot = 1; slot <= 9; slot++) {
			ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
			if (itemInSlot != null && itemInSlot.getItem() instanceof ItemFishingRod) {
				return slot;
			}
		}
		return -1;
	}
}

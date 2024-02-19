package cc.unknown.module.impl.player;

import cc.unknown.event.impl.api.EventLink;
import cc.unknown.event.impl.player.TickEvent;
import cc.unknown.module.Module;
import cc.unknown.module.impl.ModuleCategory;
import cc.unknown.module.setting.impl.BooleanValue;
import cc.unknown.module.setting.impl.SliderValue;
import cc.unknown.utils.player.PlayerUtil;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;

public class FastPlace extends Module {
	private SliderValue delaySlider = new SliderValue("Delay", 0.0D, 0.0D, 4.0D, 1.0D);
	private BooleanValue blockOnly = new BooleanValue("Blocks only", true);
	private BooleanValue projSeparate = new BooleanValue("Separate Projectile Delay", true);
	private SliderValue projSlider = new SliderValue("Projectile Delay", 2.0D, 0.0D, 4.0D, 1.0D);
	
	public FastPlace() {
		super("FastPlace", ModuleCategory.Player);
		this.registerSetting(delaySlider, blockOnly, projSeparate, projSlider);
	}

    @Override
    public boolean canBeEnabled() {
        return mc.rightClickDelayTimer != 4;
    }

    @EventLink
    public void onPlayerTick(TickEvent e) {
        if (PlayerUtil.inGame() && mc.inGameHasFocus) {
            ItemStack item = mc.thePlayer.getHeldItem();

            if (blockOnly.isToggled() && item != null) {
                if (item.getItem() instanceof ItemBlock) {
                	rightDelay(delaySlider.getInputToInt());
                } else if ((item.getItem() instanceof ItemSnowball || item.getItem() instanceof ItemEgg) && projSeparate.isToggled()) {
                	rightDelay(projSlider.getInputToInt());
                }
            } else {
            	rightDelay(delaySlider.getInputToInt());
            }
        }
    }
    
    private void rightDelay(int x) {
        if (x == 0) {
            mc.rightClickDelayTimer = 0;
        } else if (x != 4 && mc.rightClickDelayTimer == 4) {
            mc.rightClickDelayTimer = x;
        }
    }
}
package cc.unknown.ui.clickgui.raven;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import cc.unknown.Haru;
import cc.unknown.module.impl.ModuleCategory;
import cc.unknown.module.impl.visuals.ClickGuiModule;
import cc.unknown.ui.clickgui.raven.impl.CategoryComp;
import cc.unknown.ui.clickgui.raven.impl.api.Component;
import cc.unknown.ui.clickgui.raven.theme.Theme;
import cc.unknown.utils.client.FuckUtil;
import cc.unknown.utils.client.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class ClickGui extends GuiScreen {
	private final ArrayList<CategoryComp> categoryList = new ArrayList<>();
	private final Map<String, ResourceLocation> waifuMap = new HashMap<>();
	private ClickGuiModule cg = (ClickGuiModule) Haru.instance.getModuleManager().getModule(ClickGuiModule.class);
	private ResourceLocation waifuImage = waifuMap.get(cg.waifuMode.getMode().toLowerCase());
	private ScaledResolution sr = new ScaledResolution(mc);

	private boolean isDragging = false;
	private AtomicInteger lastMouseX = new AtomicInteger(0);
	private AtomicInteger lastMouseY = new AtomicInteger(0);

	public ClickGui() {
		int topOffset = 5;
		for (ModuleCategory category : ModuleCategory.values()) {
		    CategoryComp comp = new CategoryComp(category);
		    comp.setY(topOffset);
		    categoryList.add(comp);
		    topOffset += 20;
		}

		String[] waifuNames = { "astolfo", "hideri", "manolo", "bunny", "kurumi", "uzaki", "fujiwara", "cat", "megumin", "komi" };
		Arrays.stream(waifuNames).forEach(name -> waifuMap.put(name, new ResourceLocation("haru/img/clickgui/" + name + ".png")));
	}

	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		for (CategoryComp category : categoryList) {
		    category.render(this.fontRendererObj);
		    category.updste(mouseX, mouseY);

		    for (Component module : category.getModules()) {
		        module.update(mouseX, mouseY);
		    }
		}
		
		if (cg.gradient.isToggled()) {
			RenderUtil.drawGradientRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(),
					Theme.getMainColor().getRGB(), Theme.getMainColor().getAlpha());
		}
		
	    if (waifuImage != null && cg.importUr.isToggled()) {
	        new Thread(() -> {
	            try {
	                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	            } catch (Exception e) {
	                e.printStackTrace();
	                return;
	            }

	            JFileChooser chooser = new JFileChooser();
	            FileNameExtensionFilter filter = new FileNameExtensionFilter("Import Ur Waifu", "jpg", "png");
	            chooser.setFileFilter(filter);

	            int returnVal = chooser.showOpenDialog(null);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	            	File selectedFile = chooser.getSelectedFile();
	                try {
	                    BufferedImage image = ImageIO.read(selectedFile);
	                    waifuImage = mc.getTextureManager().getDynamicTextureLocation("waifu", new DynamicTexture(image));
	                    RenderUtil.drawImage(waifuImage, FuckUtil.instance.getWaifuX(), FuckUtil.instance.getWaifuY(),
	                            sr.getScaledWidth() / 5.2f, sr.getScaledHeight() / 2f);
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }).start();
	    } else if (waifuImage != null) {
	        RenderUtil.drawImage(waifuImage, FuckUtil.instance.getWaifuX(), FuckUtil.instance.getWaifuY(),
	                sr.getScaledWidth() / 5.2f, sr.getScaledHeight() / 2f);
	    } else {
	        isDragging = false;
	    }

		if (isDragging) {
			FuckUtil.instance.setWaifuX(FuckUtil.instance.getWaifuX() + mouseX - lastMouseX.get());
			FuckUtil.instance.setWaifuY(FuckUtil.instance.getWaifuY() + mouseY - lastMouseY.get());
			lastMouseX.set(mouseX);
			lastMouseY.set(mouseY);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
	    if (isBound(mouseX, mouseY, sr) && mouseButton == 0) {
	        isDragging = true;
	        lastMouseX.set(mouseX);
	        lastMouseY.set(mouseY);
	        return;
	    }

	    for (CategoryComp category : categoryList) {
	        if (category.isInside(mouseX, mouseY) && !category.i(mouseX, mouseY)
	                && !category.mousePressed(mouseX, mouseY) && mouseButton == 0) {
	            category.mousePressed(true);
	            category.setXx(mouseX - category.getX());
	            category.setYy(mouseY - category.getY());
	        }

	        if (category.mousePressed(mouseX, mouseY) && mouseButton == 0) {
	            category.setOpened(!category.isOpened());
	        }

	        if (category.i(mouseX, mouseY) && mouseButton == 0) {
	            category.cv(!category.p());
	        }

	        if (category.isOpened() && !category.getModules().isEmpty()) {
	            for (Component module : category.getModules()) {
	                module.mouseDown(mouseX, mouseY, mouseButton);
	            }
	        }
	    }
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (state == 0) {
			if (isBound(mouseX, mouseY, sr)) {
				isDragging = false;
				return;
			}

	        for (CategoryComp category : categoryList) {
	            category.mousePressed(false);
	            if (category.isOpened() && !category.getModules().isEmpty()) {
	                for (Component module : category.getModules()) {
	                    module.mouseReleased(mouseX, mouseY, state);
	                }
	            }
	        }
	    }

	    if (Haru.instance.getClientConfig() != null) {
	        Haru.instance.getClientConfig().saveConfig();
	    }

	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
	    if (keyCode == 54 || keyCode == 1) {
	        mc.displayGuiScreen(null);
	    } else {
	        for (CategoryComp category : categoryList) {
	            if (category.isOpened() && !category.getModules().isEmpty()) {
	                for (Component module : category.getModules()) {
	                    module.keyTyped(typedChar, keyCode);
	                }
	            }
	        }
	    }
	}

	@Override
	public void onGuiClosed() {
		if (cg != null && cg.isEnabled() && Haru.instance.getClientConfig() != null) {
			Haru.instance.getClientConfig().saveConfig();
			cg.disable();
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	public ArrayList<CategoryComp> getCategoryList() {
		return categoryList;
	}

	private boolean isBound(int x, int y, ScaledResolution sr) {
		return x >= FuckUtil.instance.getWaifuX() && x <= FuckUtil.instance.getWaifuX() + (sr.getScaledWidth() / 5.1f)
				&& y >= FuckUtil.instance.getWaifuY()
				&& y <= FuckUtil.instance.getWaifuY() + (sr.getScaledHeight() / 2f);
	}

}

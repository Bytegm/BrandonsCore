package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import codechicken.lib.math.MathHelper;
import codechicken.lib.vec.Cuboid6;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.brandonscore.client.ResourceHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
import com.brandon3055.brandonscore.client.utils.GuiHelperOld;
import com.brandon3055.brandonscore.lib.DLRSCache;
import com.brandon3055.brandonscore.lib.DLResourceLocation;
import com.brandon3055.brandonscore.lib.ScissorHelper;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by brandon3055 on 5/31/2018.
 */
public class ImageElement extends MDElementBase<ImageElement> {

    private static final Random rand = new Random();
    private static final List<Block> LOADING_BLOCKS = Lists.newArrayList(Blocks.STONE, Blocks.SAND, Blocks.GRASS, Blocks.COBBLESTONE, Blocks.OAK_LOG, Blocks.GLASS, Blocks.MYCELIUM, Blocks.CHEST, Blocks.ENCHANTING_TABLE, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BEACON, Blocks.BOOKSHELF, Blocks.DIAMOND_ORE, Blocks.OBSIDIAN, Blocks.DIRT, Blocks.DISPENSER, Blocks.FURNACE, Blocks.HAY_BLOCK);
    private ItemStack renderLoadingStack = ItemStack.EMPTY;
    private int loadingTime = 0;
    private int maxLoadTime = 40;
    private boolean downloading = false;

    private MDElementContainer container;
    private String imageURL;
    private DLResourceLocation resourceLocation;
    public String linkTo = "";

    public ImageElement(MDElementContainer container, String imageURL) {
        this.container = container;
        this.imageURL = imageURL;
    }

    @Override
    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
        resourceLocation = DLRSCache.getResource(imageURL);
        int w = 0;
        int h = 0;

        if (!resourceLocation.dlFinished || resourceLocation.dlFailed) {
            w = 76;
            h = 76;
            downloading = true;
        }
        else {
            if (width == -1 && height == -1) {
                width = 32;
            }
            if (width != -1) {
                w = screenRelativeSize ? (int) (MathHelper.clip(width / 100D, 0, 1) * layout.getWidth()) : MathHelper.clip(width, 8, layout.getWidth());
                if (height == -1) {
                    if (resourceLocation.sizeSet) {
                        h = (int) (((double) resourceLocation.height / (double) resourceLocation.width) * w);
                    }
                    else {
                        h = w;
                    }
                }
            }
            if (height != -1) {
                h = height;
                if (width == -1) {
                    if (resourceLocation.sizeSet) {
                        w = (int) (((double) resourceLocation.width / (double) resourceLocation.height) * height);
                    }
                    else {
                        w = height;
                    }
                }
            }
        }

        setSize(w, h);
        super.layoutElement(layout, lineElement);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1, 1, 1, 1);
        if (downloading) {
            renderDownloading(partialTicks);
        }
        else {
            ResourceHelperBC.bindTexture(resourceLocation);
            boolean mouseOver = isMouseOver(mouseX, mouseY);

            if (hasColourBorder) {
                drawColouredRect(xPos(), yPos(), xSize(), ySize(), 0xFF000000 | getColourBorder(mouseOver));
            }
            else if (hasColourBorderHover && mouseOver) {
                drawColouredRect(xPos(), yPos(), xSize(), ySize(), 0xFF000000 | colourBorderHover);
            }

            int w = xSize() - rightPad - leftPad;
            int h = ySize() - bottomPad - topPad;
            RenderSystem.enableBlend();
            container.drawModalRectWithCustomSizedTexture(xPos() + leftPad, yPos() + topPad, 0, 0, w, h, w, h);
            RenderSystem.disableBlend();
        }

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    private void renderDownloading(float partialTicks) {
        boolean failed = resourceLocation.dlFailed;
        float failTicks = failed ? 0 : partialTicks;
        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, failed ? 0xFFFF0000 : 0xFF00FF00);
        float anim = (64 + 10) * ((loadingTime + failTicks) / (float) maxLoadTime);

        bindTexture(BCSprites.MODULAR_GUI);
        float texAnim = Math.max(0F, (1 - (anim / 64)) * 48);
        float texX = xPos() + (xSize() / 2F) - 20;
        float texY = Math.max(yPos() - 48 + ((48 - texAnim) * 2), yPos());
        RenderSystem.pushMatrix();
        RenderSystem.translated(0, 0, getRenderZLevel() + 200);
        drawScaledCustomSizeModalRect(texX, texY + 1, failed ? 20 : 0, 18 + (24 - Math.min(24, 48 - texAnim)), 20, Math.min(Math.min(24, 48 - texAnim), texAnim), 40, Math.min(Math.min(48, (48 - texAnim) * 2), texAnim * 2), 256, 256);

        RenderSystem.translated(xPos() + xSize() / 2D, yPos() + ySize() - 32, 0);
        RenderSystem.rotatef((BCClientEventHandler.elapsedTicks + partialTicks) * 3F, 0, 1, 0);
        RenderSystem.scaled(-64, -64, -64);
        RenderSystem.rotatef(-30, 1, 0, 0);
        RenderSystem.rotatef(45, 0, 1, 0);
        ScissorHelper.pushGuiScissor(mc, xPos(), maxYPos() - anim, xSize(), anim, screenWidth, screenHeight);
        RenderHelper.turnBackOn();
        RenderSystem.pushMatrix();
        double shrink = 1 - MathHelper.clip((anim - 64) / 10D, 0, 1);
        RenderSystem.scaled(shrink, shrink, shrink);
        mc.getItemRenderer().renderGuiItem(renderLoadingStack, 0, 0);//, ItemCameraTransforms.TransformType.FIXED);
        RenderSystem.popMatrix();
        RenderHelper.turnOff();
        ScissorHelper.popScissor();

        Cuboid6 cuboid6 = new Cuboid6(-0.251, -0.251, -0.251, 0.251, 0.251, 0.251);
        RenderSystem.disableTexture();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        float r = 1, g = 1, b = 1;
        if (failed) {
            r = 0; g = 0; b = 0;
        }

        GuiHelperOld.renderCuboid(cuboid6, r, g, b, 1);

        RenderSystem.enableTexture();

        //TODO font renderer changes
//        RenderSystem.color4f(fontRenderer.red, fontRenderer.blue, fontRenderer.green, 1);
        RenderSystem.popMatrix();
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOver(mouseX, mouseY)) {
            if (!linkTo.isEmpty() && container.linkDisplayTarget != null) {
                GuiElement e = container.linkDisplayTarget;
                int width = fontRenderer.width(linkTo);
                int height = fontRenderer.wordWrapHeight(linkTo, e.xSize()) + 4;
                zOffset += container.linkDisplayZOffset;
                drawColouredRect(e.xPos(), e.maxYPos() - height, Math.min(Math.max(width + 4, e.xSize() / 2), e.xSize()), height, 0x90000000);
                drawSplitString(fontRenderer, linkTo, e.xPos() + 2, e.maxYPos() - height + 2, e.xSize(), 0xc0c0c0, false);
                zOffset -= container.linkDisplayZOffset;
            }

            List<String> tooltip = new ArrayList<>();
            if (resourceLocation.dlFailed) {
                tooltip.add(TextFormatting.RED + I18n.get("gui.bc.downloading_image_failed.info"));
            }
            else if (!resourceLocation.dlFinished) {
                tooltip.add(TextFormatting.GREEN + I18n.get("gui.bc.downloading_image.info"));
            }

            if (enableTooltip && !this.tooltip.isEmpty()) {
                tooltip.addAll(this.tooltip);
            }

            if (!tooltip.isEmpty()) {
                drawHoveringText(tooltip, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                return true;
            }
        }
        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseButton == 0 && Screen.hasShiftDown()) {
                DLRSCache.clearResourceCache(imageURL);
                DLRSCache.clearFileCache(imageURL);
                container.layoutMarkdownElements();
                loadingTime = 0;
                return true;
            }
            else if (mouseButton != 1 && !linkTo.isEmpty()) {
                container.handleLinkClick(linkTo, mouseButton);
                return true;
            }
            else if (mouseButton == 1) {
                container.handleLinkClick(imageURL, mouseButton);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onUpdate() {
        if (resourceLocation != null) {
            if (!resourceLocation.dlFinished || resourceLocation.dlFailed || loadingTime > 0) {
                if (loadingTime == 0) {
                    renderLoadingStack = new ItemStack(LOADING_BLOCKS.get(rand.nextInt(LOADING_BLOCKS.size())));
                }
                loadingTime++;

                if (resourceLocation.dlFailed && loadingTime == maxLoadTime / 2) {
                    loadingTime = (maxLoadTime / 2) - 1;
                }

                if (loadingTime >= maxLoadTime) {
                    loadingTime = 0;
                }

                if (resourceLocation.dlStateChanged()) {
                    container.getTopLevelContainer().layoutMarkdownElements();
                    downloading = false;
                    return true;
                }
            }
            else if (downloading){
                loadingTime = 0;
                downloading = false;
                container.getTopLevelContainer().layoutMarkdownElements();
                return true;
            }
        }

        return super.onUpdate();
    }
}

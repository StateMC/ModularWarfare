package com.modularwarfare.client.gui.customization;

import com.google.gson.JsonSyntaxException;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.gui.api.GuiMWContainer;
import com.modularwarfare.client.gui.api.GuiMWScreen;
import com.modularwarfare.client.gui.api.GuiUtils;
import com.modularwarfare.client.gui.customization.containers.ContainerGunParts;
import com.modularwarfare.client.model.renders.RenderParameters;
import com.modularwarfare.client.shader.ProjectionHelper;
import com.modularwarfare.common.guns.ItemGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;


public class GuiMainScreen extends GuiMWScreen {

    public ItemStack gunStack;
    public ItemGun gun;
    public ContainerGunParts containerGunParts = new ContainerGunParts(0, 10, 10, 100, 170, this);
    private float zoom = 1.0f;

    private static final ProjectionHelper projectionHelper = new ProjectionHelper();

    public GuiMainScreen(ItemStack gunStack, ItemGun gun) {
        this.gunStack = gunStack;
        this.gun = gun;
    }

    @Override
    public void initGui() {
        super.initGui();
        Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation("modularwarfare:shaders/post/blur.json"));
        addContainer(this.containerGunParts);
    }

    @Override
    public void onGuiClosed() {
        containerList.forEach(GuiMWContainer::onClose);
        Minecraft.getMinecraft().entityRenderer.stopUseShader();
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            float inc = zoom + dWheel/120 * 0.01f;
            zoom = MathHelper.clamp(inc, 1.0f, 1.75f);
            ModularWarfare.LOGGER.info(zoom);
        }
    }

    private static Vec3d getMouseVector(float z) {
        return projectionHelper.unproject(Mouse.getX(), Mouse.getY(), z);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        float MENU_ROTATION = (width - mouseX) / 8 - 80;
        float MENU_ROTATION_2 = (height - mouseY) / 12;
        float angleY = (float) (-10 + (-MENU_ROTATION / 2) + (9.0f * Math.sin(RenderParameters.SMOOTH_SWING / 80)));
        float angleX = (float) ((-MENU_ROTATION_2 / 2) + 10 + (6.0f * Math.cos(RenderParameters.SMOOTH_SWING / 80)));

        float backgroundGradientSwing = (float) ((Math.sin(RenderParameters.SMOOTH_SWING / 80) + 1) * 100);

        // Render text & extras
        GlStateManager.pushMatrix();

        drawContainers(mouseX, mouseY, partialTicks);

        GuiUtils.renderRectWithGradient(0, 0, width + (int) backgroundGradientSwing, height + (int) backgroundGradientSwing, 0x33737373, 0x66000000, 0);
        GuiUtils.renderCenteredTextScaledWithOutline("- Weapon Modding -", this.width/2, 15, 0xFFFFFF, 0xFF262626, 2.5);
        GuiUtils.renderCenteredTextScaled(gun.type.displayName+" - "+gun.type.weaponType.typeName, this.width/2, 40, 0xFFFFFF,1);
        GuiUtils.renderTextScaled("\u00a9 Copyright - ModularWarfare", 3, height - 7, 0xFFFFFFFF, 0.5);
        GuiUtils.renderTextScaled("https://modularmods.net", width - 65, height - 7, 0xFFFFFFFF, 0.5);
        GuiUtils.renderTextScaledWithOutline("EXIT >", width - 90, height - 45, 0xFFFFFFFF, 0xFF262626,2.5);

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        {
            GlStateManager.pushMatrix();
            {

                GlStateManager.enableRescaleNormal();
                GlStateManager.enableAlpha();
                GlStateManager.alphaFunc(516, 0.1F);
                RenderHelper.enableStandardItemLighting();
                GlStateManager.enableLighting();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.enableDepth();

                GL11.glTranslatef(500.0F, 210.0F, 50.0F);
                GlStateManager.scale(-1.0f, 1.0f, 1.0f);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

                //Mouse rotation
                GlStateManager.rotate(angleX, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(angleY/3, 0.0F, 1.0F, 0.0F);

                GlStateManager.scale(zoom, zoom, 1.0f);


                GlStateManager.scale(1.75f, 1.75f, 1.0f);
                GlStateManager.scale(200F, 200F, 200F);

                for(String part : this.containerGunParts.partsSets.keySet()){
                    if(this.containerGunParts.partsSets.get(part)){
                        ClientRenderHooks.customRenderers[gun.type.id].bindTexture(gun.type.getAssetDir(), gun.type.modelSkins[0].getSkin());
                        gun.type.model.renderPart(part, 1/16F);
                    }
                }

                GlStateManager.disableAlpha();
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableLighting();

                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
                GlStateManager.disableBlend();
            }

            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return true;
    }


}

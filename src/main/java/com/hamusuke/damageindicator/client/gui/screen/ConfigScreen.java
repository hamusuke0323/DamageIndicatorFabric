package com.hamusuke.damageindicator.client.gui.screen;

import com.hamusuke.damageindicator.DamageIndicator;
import com.hamusuke.damageindicator.client.DamageIndicatorClient;
import com.hamusuke.damageindicator.client.gui.widget.Slider;
import com.hamusuke.damageindicator.client.renderer.IndicatorRenderer;
import com.hamusuke.damageindicator.config.values.RGBValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {
    private static final Text HIDE_INDICATOR = new TranslatableText("options.damageindicator.hideIndicator");
    private static final Text FORCE_INDICATOR_RENDERING = new TranslatableText("options.damageindicator.forceindicatorrendering");
    private static final Text CHANGE_COLOR_WHEN_CRIT = new TranslatableText("options.damageindicator.changeColorWhenCrit");
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(new TranslatableText("options.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height / 2 - 50, this.width / 2, 20, ScreenTexts.composeToggleText(HIDE_INDICATOR, DamageIndicatorClient.clientConfig.hideIndicator.get()), p_onPress_1_ -> {
            DamageIndicatorClient.clientConfig.hideIndicator.toggle();
            p_onPress_1_.setMessage(ScreenTexts.composeToggleText(HIDE_INDICATOR, DamageIndicatorClient.clientConfig.hideIndicator.get()));
        }));

        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height / 2 - 30, this.width / 2, 20, ScreenTexts.composeToggleText(FORCE_INDICATOR_RENDERING, DamageIndicatorClient.clientConfig.forciblyRenderIndicator.get()), p_onPress_1_ -> {
            DamageIndicatorClient.clientConfig.forciblyRenderIndicator.toggle();
            p_onPress_1_.setMessage(ScreenTexts.composeToggleText(FORCE_INDICATOR_RENDERING, DamageIndicatorClient.clientConfig.forciblyRenderIndicator.get()));
        }));

        this.addDrawableChild(new Slider(this.width / 4, this.height / 2 - 10, this.width / 2, 20, new TranslatableText("options.damageindicator.displayDistance"), DamageIndicatorClient.clientConfig.renderDistance.getMin(), DamageIndicatorClient.clientConfig.renderDistance.getMax(), DamageIndicatorClient.clientConfig.renderDistance.get(), slider -> DamageIndicatorClient.clientConfig.renderDistance.set(slider.getIntValue())));

        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height / 2 + 10, this.width / 2, 20, ScreenTexts.composeToggleText(CHANGE_COLOR_WHEN_CRIT, DamageIndicatorClient.clientConfig.changeColorWhenCrit.get()), p_onPress_1_ -> {
            DamageIndicatorClient.clientConfig.changeColorWhenCrit.toggle();
            p_onPress_1_.setMessage(ScreenTexts.composeToggleText(CHANGE_COLOR_WHEN_CRIT, DamageIndicatorClient.clientConfig.changeColorWhenCrit.get()));
        }));

        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height / 2 + 30, this.width / 2, 20, new TranslatableText(DamageIndicator.MOD_ID + ".config.colorConfig.title"), p_onPress_1_ -> this.client.setScreen(new ColorSettingsScreen(this))));

        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height - 20, this.width / 2, 20, ScreenTexts.DONE, p_onPress_1_ -> this.onClose()));
    }

    @Override
    public void render(MatrixStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
        this.renderBackground(p_96562_);
        drawCenteredText(p_96562_, this.textRenderer, this.getTitle(), this.width / 2, 10, 16777215);
        super.render(p_96562_, p_96563_, p_96564_, p_96565_);
    }

    @Override
    public void removed() {

        DamageIndicatorClient.clientConfig.save();
        DamageIndicatorClient.queue.forEach(IndicatorRenderer::syncIndicatorColor);
    }

    @Override
    public void onClose() {
        this.client.setScreen(this.parent);
    }

    @Environment(EnvType.CLIENT)
    private static class ColorSettingsScreen extends Screen {
        @Nullable
        private final Screen parent;
        private ColorList list;

        private ColorSettingsScreen(@Nullable Screen parent) {
            super(new TranslatableText(DamageIndicator.MOD_ID + ".config.colorConfig.title"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            super.init();
            double amount = this.list != null ? this.list.getScrollAmount() : 0.0D;
            this.list = new ColorList();
            this.list.setScrollAmount(amount);
            this.addSelectableChild(this.list);
            this.addDrawableChild(new ButtonWidget(this.width / 2 - this.width / 4, this.height - 20, this.width / 2, 20, ScreenTexts.DONE, p_onPress_1_ -> this.onClose()));
        }

        @Override
        public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
            this.list.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
            super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        }

        @Override
        public void onClose() {
            this.client.setScreen(this.parent);
        }

        @Environment(EnvType.CLIENT)
        private static class ColorMixingScreen extends Screen {
            @Nullable
            private final Screen parent;
            private final RGBValue rgb;
            private Slider red;
            private Slider green;
            private Slider blue;

            private ColorMixingScreen(@Nullable Screen parent, RGBValue rgb) {
                super(new TranslatableText(DamageIndicator.MOD_ID + ".config.color." + rgb.getName()));
                this.parent = parent;
                this.rgb = rgb;
            }

            @Override
            protected void init() {
                super.init();

                this.red = this.addDrawableChild(new Slider(this.width / 4, this.height / 2 - 70, this.width / 2, 20, Text.of("Red: "), 0.0D, 255.0D, this.rgb.get().getRed(), slider -> {
                }));
                this.green = this.addDrawableChild(new Slider(this.width / 4, this.height / 2 - 45, this.width / 2, 20, Text.of("Green: "), 0.0D, 255.0D, this.rgb.get().getGreen(), slider -> {
                }));
                this.blue = this.addDrawableChild(new Slider(this.width / 4, this.height / 2 - 20, this.width / 2, 20, Text.of("Blue: "), 0.0D, 255.0D, this.rgb.get().getBlue(), slider -> {
                }));
                this.addDrawableChild(new ButtonWidget(this.width / 2 - this.width / 4, this.height - 20, this.width / 2, 20, ScreenTexts.DONE, p_onPress_1_ -> this.onClose()));
            }

            @Override
            public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
                this.renderBackground(p_230430_1_);
                drawCenteredText(p_230430_1_, this.textRenderer, this.title, this.width / 2, 5, 16777215);
                int color = MathHelper.packRgb(this.red.getIntValue(), this.green.getIntValue(), this.blue.getIntValue()) + (255 << 24);
                this.fillGradient(p_230430_1_, this.width / 4, this.height / 2 + 5, this.width * 3 / 4, this.height - 25, color, color);
                super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
            }

            @Override
            public void removed() {
                super.removed();
                this.rgb.get().setRed(this.red.getIntValue());
                this.rgb.get().setGreen(this.green.getIntValue());
                this.rgb.get().setBlue(this.blue.getIntValue());
                DamageIndicatorClient.clientConfig.save();
            }

            @Override
            public void onClose() {
                this.client.setScreen(this.parent);
            }
        }

        @Environment(EnvType.CLIENT)
        private class ColorList extends ElementListWidget<ColorList.Color> {
            public ColorList() {
                super(ColorSettingsScreen.this.client, ColorSettingsScreen.this.width, ColorSettingsScreen.this.height, 20, ColorSettingsScreen.this.height - 20, 20);
                for (RGBValue rgb : DamageIndicatorClient.clientConfig.getRGBConfigs()) {
                    this.addEntry(new Color(rgb));
                }
            }

            @Environment(EnvType.CLIENT)
            private class Color extends ElementListWidget.Entry<Color> {
                private final ButtonWidget button;

                private Color(RGBValue rgbConfig) {
                    this.button = new ButtonWidget(ColorSettingsScreen.this.width / 4, 0, ColorSettingsScreen.this.width / 2, 20, new TranslatableText(DamageIndicator.MOD_ID + ".config.color." + rgbConfig.getName()), p_onPress_1_ -> ColorSettingsScreen.this.client.setScreen(new ColorMixingScreen(ColorSettingsScreen.this, rgbConfig)));
                }

                @Override
                public void render(MatrixStack p_230432_1_, int p_230432_2_, int p_230432_3_, int p_230432_4_, int p_230432_5_, int p_230432_6_, int p_230432_7_, int p_230432_8_, boolean p_230432_9_, float p_230432_10_) {
                    this.button.y = p_230432_3_;
                    this.button.render(p_230432_1_, p_230432_7_, p_230432_8_, p_230432_10_);
                }

                @Override
                public List<? extends Element> children() {
                    return Collections.singletonList(this.button);
                }

                @Override
                public List<? extends Selectable> selectableChildren() {
                    return Collections.singletonList(this.button);
                }
            }
        }
    }
}

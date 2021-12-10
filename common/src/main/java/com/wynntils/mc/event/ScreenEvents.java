package com.wynntils.mc.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.ItemUtils;
import com.wynntils.WynntilsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.Consumer;

public class ScreenEvents {

    public static void onScreenCreated(Screen screen, List<AbstractWidget> buttons, Consumer<AbstractWidget> addButton) {
        System.out.println("DEBUG: onScreenCreated");
        if (screen instanceof TitleScreen titleScreen) {
            WynntilsMod.postTitleScreenInit(titleScreen, buttons, addButton);
        } else if (screen instanceof PauseScreen gameMenuScreen) {
            WynntilsMod.postGameMenuScreenInit(gameMenuScreen, buttons);
        }
    }

    public static void onInventoryRender(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTicks, Slot hoveredSlot) {
        System.out.println("DEBUG: onInventoryRender");
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        replaceLore(hoveredSlot.getItem());
    }

    public static void onTooltipRender(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
        // this is done for inventory only. But why?
        GlStateManager._translated(0, 0, -300d);
    }

    private static void replaceLore(ItemStack stack) {
        // Soul Point Timer
        if ((stack.getItem() == Items.NETHER_STAR || stack.getItem() == Item.byBlock(Blocks.SNOW)) && stack.getDisplayName().getString().contains("Soul Point")) {
            List<String> lore = ItemUtils.getLore(stack);
            if (lore != null && !lore.isEmpty()) {
                if (lore.get(lore.size() - 1).contains("Time until next soul point: ")) {
                    lore.remove(lore.size() - 1);
                    lore.remove(lore.size() - 1);
                }
            }

            lore.add("");
            int secondsUntilSoulPoint = 900; //FIXME: PlayerInfo.get(InventoryData.class).getTicksToNextSoulPoint() / 20;
            int minutesUntilSoulPoint = secondsUntilSoulPoint / 60;
            secondsUntilSoulPoint %= 60;
            lore.add(ChatFormatting.AQUA + "Time until next soul point: " + ChatFormatting.WHITE + minutesUntilSoulPoint + ":" + String.format("%02d", secondsUntilSoulPoint));
            ItemUtils.replaceLore(stack, lore);
            return;

        }

        /*
        // Wynnic Translator
        if (stack.hasTagCompound() && !stack.getTag().getBoolean("showWynnic") && Utils.isKeyDown(GLFW.GLFW_KEY_LSHIFT)) {
            String fullLore = ItemUtils.getStringLore(stack);
            if (StringUtils.hasWynnic(fullLore) || StringUtils.hasGavellian(fullLore)) {
                ListNBT loreList = ItemUtils.getLoreTag(stack);
                if (loreList != null) {
                    stack.getTag().put("originalLore", loreList.copy());
                    boolean capital = true;
                    for (int index = 0; index < loreList.size(); index++) {
                        String lore = loreList.getString(index);
                        if (StringUtils.hasWynnic(lore) || StringUtils.hasGavellian(lore)) {
                            StringBuilder translated = new StringBuilder();
                            boolean colorCode = false;
                            StringBuilder number = new StringBuilder();
                            for (char character : lore.toCharArray()) {
                                if (StringUtils.isWynnicNumber(character)) {
                                    number.append(character);
                                } else {
                                    if (!number.toString().isEmpty()) {
                                        translated.append(StringUtils.translateNumberFromWynnic(number.toString()));
                                        number = new StringBuilder();
                                    }

                                    String translatedCharacter;
                                    if (StringUtils.isWynnic(character)) {
                                        translatedCharacter = StringUtils.translateCharacterFromWynnic(character);
                                        if (capital && translatedCharacter.matches("[a-z]")) {
                                            translatedCharacter = String.valueOf(Character.toUpperCase(translatedCharacter.charAt(0)));
                                        }
                                    } else if (StringUtils.isGavellian(character)) {
                                        translatedCharacter = StringUtils.translateCharacterFromGavellian(character);
                                        if (capital) {
                                            translatedCharacter = String.valueOf(Character.toUpperCase(translatedCharacter.charAt(0)));
                                        }
                                    } else {
                                        translatedCharacter = String.valueOf(character);
                                    }

                                    translated.append(translatedCharacter);

                                    if (".?!".contains(translatedCharacter)) {
                                        capital = true;
                                    } else if (translatedCharacter.equals("§")) {
                                        colorCode = true;
                                    } else if (!translatedCharacter.equals(" ") && !colorCode) {
                                        capital = false;
                                    } else if (colorCode) {
                                        colorCode = false;
                                    }
                                }
                            }
                            if (!number.toString().isEmpty()) {
                                translated.append(StringUtils.translateNumberFromWynnic(number.toString()));
                                number = new StringBuilder();
                            }

                            loreList.set(index, StringNBT.valueOf(translated.toString()));
                        }
                    }
                }
            }
            stack.getTag().putBoolean("showWynnic", true);
        }

        if (stack.hasTagCompound() && stack.getTag().getBoolean("showWynnic") && !Utils.isKeyDown(GLFW.GLFW_KEY_LSHIFT)) {
            CompoundNBT tag = stack.getTag();
            if (tag.contains("originalLore")) {
                CompoundNBT displayTag = tag.getCompound("display");
                if (displayTag != null) {
                    displayTag.put("Lore", tag.get("originalLore"));
                }
                tag.removeTag("originalLore");
            }
            stack.getTag().putBoolean("showWynnic", false);
        }

         */
    }
}

package net.satisfy.camping.util;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextUtils
{
    public static List<Text> getTranslatedSplittedText(String translationId, @Nullable Formatting style)
    {
        MutableText text = Text.translatable(translationId);

        if(text.getString().contains("\n"))
        {
            String[] translatedSplitted = I18n.translate(translationId).split("\n");
            List<Text> list = new ArrayList<>();
            Arrays.stream(translatedSplitted).forEach(s -> list.add(style == null ? Text.literal(s) : Text.literal(s).formatted(style)));
            return list;
        }
        return Arrays.asList(style == null ? text : text.formatted(style));
    }
}
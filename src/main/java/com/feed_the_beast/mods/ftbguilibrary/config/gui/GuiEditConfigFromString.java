package com.feed_the_beast.mods.ftbguilibrary.config.gui;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigCallback;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.TextBox;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class GuiEditConfigFromString<T> extends GuiBase
{
	public static <E> void open(ConfigFromString<E> type, @Nullable E value, @Nullable E defaultValue, ConfigCallback callback)
	{
		ConfigGroup group = new ConfigGroup("group");
		group.add("value", type, value, e -> {}, defaultValue);
		new GuiEditConfigFromString<>(type, callback).openGui();
	}

	private final ConfigFromString<T> config;
	private final ConfigCallback callback;
	private T current;

	private final Button buttonCancel, buttonAccept;
	private final TextBox textBox;

	public GuiEditConfigFromString(ConfigFromString<T> c, ConfigCallback cb)
	{
		setSize(230, 54);
		config = c;
		callback = cb;
		current = config.value == null ? null : config.copy(config.value);

		int bsize = width / 2 - 10;

		buttonCancel = new SimpleTextButton(this, new TranslationTextComponent("gui.cancel"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				callback.save(false);
			}

			@Override
			public boolean renderTitleInCenter()
			{
				return true;
			}
		};

		buttonCancel.setPosAndSize(8, height - 24, bsize, 16);

		buttonAccept = new SimpleTextButton(this, new TranslationTextComponent("gui.accept"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				config.setCurrentValue(current);
				callback.save(true);
			}

			@Override
			public WidgetType getWidgetType()
			{
				return config.getCanEdit() && textBox.isTextValid() ? super.getWidgetType() : WidgetType.DISABLED;
			}

			@Override
			public boolean renderTitleInCenter()
			{
				return true;
			}
		};

		buttonAccept.setPosAndSize(width - bsize - 8, height - 24, bsize, 16);

		textBox = new TextBox(this)
		{
			@Override
			public boolean allowInput()
			{
				return config.getCanEdit();
			}

			@Override
			public boolean isValid(String txt)
			{
				return config.parse(null, txt);
			}

			@Override
			public void onTextChanged()
			{
				config.parse(t -> {
					current = t;
					textColor = config.getColor(t);
				}, getText());
			}

			@Override
			public void onEnterPressed()
			{
				if (config.getCanEdit())
				{
					buttonAccept.onClicked(MouseButton.LEFT);
				}
			}
		};

		textBox.setPosAndSize(8, 8, width - 16, 16);
		textBox.setText(config.getStringFromValue(current));
		textBox.textColor = config.getColor(current);
		textBox.setCursorPosition(textBox.getText().length());
		textBox.setFocused(true);
	}

	@Override
	public boolean onClosedByKey(Key key)
	{
		if (super.onClosedByKey(key))
		{
			config.setCurrentValue(current);
			callback.save(true);
			return false;
		}

		return false;
	}

	@Override
	public void addWidgets()
	{
		add(buttonCancel);
		add(buttonAccept);
		add(textBox);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		Screen screen = getPrevScreen();
		return screen != null && screen.isPauseScreen();
	}
}
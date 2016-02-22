package org.cloudcoder.app.wizard.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.StringValue;

public class StringValueField extends JPanel implements IPageField, UIConstants {
	private static final int HEIGHT = 32;

	private static final long serialVersionUID = 1L;
	
	private StringValue value;
	private JLabel label;
	private JTextField textField;
	
	public StringValueField() {
		label = new JLabel("", SwingConstants.RIGHT);
		label.setPreferredSize(new Dimension(200, HEIGHT));
		add(label);
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(420, 32));
		add(textField);
		markValid();
	}
	
	public void setValue(StringValue value) {
		this.value = value;
		label.setText(value.getLabel());
		textField.setText(value.getString());
	}

	@Override
	public Component asComponent() {
		return this;
	}

	@Override
	public int getFieldHeight() {
		return HEIGHT + 8;
	}

	@Override
	public void markValid() {
		textField.setBackground(Color.WHITE);
	}
	
	@Override
	public void markInvalid() {
		textField.setBackground(INVALID_FIELD_BG);
	}
	
	@Override
	public IValue getCurrentValue() {
		IValue cur = value.clone();
		cur.setString(textField.getText());
		return cur;
	}
}

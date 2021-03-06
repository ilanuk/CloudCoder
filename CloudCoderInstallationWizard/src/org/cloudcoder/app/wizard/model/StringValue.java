package org.cloudcoder.app.wizard.model;

public class StringValue extends AbstractValue implements IValue {
	private String value;
	
	public StringValue(String name, String label) {
		this(name, label, "");
	}
	
	public StringValue(String name, String label, String defValue) {
		super(name, label);
		value = defValue;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.STRING;
	}

	@Override
	public void setString(String value) {
		this.value = value;
	}

	@Override
	public <T extends Enum<T>> void setEnum(T value) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public void setBoolean(boolean value) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public void setPropertyValue(String propValue) {
		this.value = propValue;
	}
	
	@Override
	public void update(IValue otherValue) {
		if (otherValue.getClass() != this.getClass()) {
			throw new IllegalArgumentException("Cannot update " + this.getClass().getSimpleName() +
					" from " + otherValue.getClass().getSimpleName());
		}
		this.value = ((StringValue)otherValue).value;
	}

	@Override
	public String getString() {
		return this.value;
	}
	
	@Override
	public boolean getBoolean() {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> T getEnum(Class<T> cls) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public String getPropertyValue() {
		return value;
	}
	
	@Override
	public StringValue clone() {
		try {
			return (StringValue) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should not happen");
		}
	}
}

package org.cloudcoder.app.wizard.model;

public class BooleanValue extends AbstractValue implements IValue {
	private boolean value;
	
	public BooleanValue(String name, String label) {
		super(name, label);
	}

	public BooleanValue(String name, String label, boolean defValue) {
		super(name, label);
		this.value = defValue;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.BOOLEAN;
	}

	@Override
	public void setString(String value) {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> void setEnum(T value) {
		throw new IllegalArgumentException();
	}

	@Override
	public void setBoolean(boolean value) {
		this.value = value;
	}
	
	@Override
	public void setPropertyValue(String propValue) {
		this.value = Boolean.valueOf(propValue);
	}
	
	@Override
	public void update(IValue otherValue) {
		if (otherValue.getClass() != this.getClass()) {
			throw new IllegalArgumentException("Cannot update " + this.getClass().getSimpleName() +
					" from " + otherValue.getClass().getSimpleName());
		}
		this.value = ((BooleanValue)otherValue).value;
	}

	@Override
	public String getString() {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> T getEnum(Class<T> cls) {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean getBoolean() {
		return this.value;
	}
	
	@Override
	public String getPropertyValue() {
		return String.valueOf(value);
	}
	
	@Override
	public BooleanValue clone() {
		try {
			return (BooleanValue) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should not happen");
		}
	}
}

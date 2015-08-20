package tw.idv.ian.shiftArrangement;

public class Doctor {

	private int priority;
	private String name;
	private String code;

	public Doctor(int priority, String name, String code) {
		super();
		this.priority = priority;
		this.name = name;
		this.code = code;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}

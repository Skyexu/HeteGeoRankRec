package hdu.util;

public class CheckinException extends Exception {
	private static final long serialVersionUID = 8130857753582100110L;

	public CheckinException (String msg) {
		super (msg);
	}

	public CheckinException(String format, Object... args) {
		super (String.format(format, args));
	}

	public CheckinException(Exception e) {
		super.setStackTrace(e.getStackTrace());
	}
}

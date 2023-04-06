package util;

import java.util.List;
import java.util.Map;

public class OutputToUser {
	public static final String general_fail = "failed";
	public static final String general_success = "successful";
	public static final String addMovieSlotSuccess = "Movie slot added successfully!";
	public static final String addMovieSlot_MovieFromPastWeek = "Movie cannot be added as it is from past date";
	public static final String addMovieSlot_MovieAfterAWeek = "Movie slot can only be added for next week from today's date";
	public static final String addMovieSlot_WithWrongInfoOrFailure = "Some issue occured while adding a movie slot. Please check the entered data!";
	public static final String removeMovieSlot_WithWrongInfo = "Some issue occured while deleting a movie slot. Please check the entered data!";
	public static final String removeMovieSlot_WithMovieDoesNotExist = "Event is full";
	public static final String removeMovieSlot_pastDate = "Movie is from past date, user cannot delete it.";
	public static final String removeMovieSlot_success = "Movie slot deleted successfully!";

	public static String addMovieSlotMessages(boolean isSuccess, String reason) {
		if (isSuccess) {
			if (reason == null) {
				reason = addMovieSlotSuccess;
			}
		} else {
			if (reason == null) {
				reason = addMovieSlot_WithWrongInfoOrFailure;
			}
		}
		return reason;
	}

	/**
	 * Format of each string in allEventIDsWithCapacity --> EventID+ one space + remainingCapacity
	 */
	public static String listEventAvailabilityOutput(boolean isSuccess, List<String> allEventIDsWithCapacity, String reason) {
		if (isSuccess) {
			reason = general_success;
			if (allEventIDsWithCapacity.size() > 0) {
				StringBuilder reasonBuilder = new StringBuilder();
				for (String event :
						allEventIDsWithCapacity) {
					if (event.length() > 10) {
						reasonBuilder.append(event).append("@");
					}
				}
				if (reasonBuilder.length() > 0)
					reason = reasonBuilder.toString();
				if (reason.endsWith("@"))
					reason = reason.substring(0, reason.length() - 1);
			}
		} else {
			reason = general_fail;
		}
		return reason;
	}

	public static String removeEventOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return reason;
	}

	public static String bookEventOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return reason;
	}

	//Format for output EventType+ one space + EventID
	public static String getBookingScheduleOutput(boolean isSuccess, Map<String, List<String>> events, String reason) {
		if (isSuccess) {
			reason = general_success;
			if (events.size() > 0) {
				StringBuilder reasonBuilder = new StringBuilder();
				for (String eventType :
						events.keySet()) {
					for (String eventID :
							events.get(eventType)) {
						reasonBuilder.append(eventType).append(" ").append(eventID).append("@");
					}
				}
				reason = reasonBuilder.toString();
				if (!reason.equals(""))
					reason = reason.substring(0, reason.length() - 1);
			}
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return reason;
	}

	public static String cancelEventOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return reason;
	}


	public static String swapEventOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return reason;
	}
}

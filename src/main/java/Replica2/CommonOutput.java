package Replica5;

import java.util.List;
import java.util.Map;

public class CommonOutput {
	public static final String general_fail = "failed";
	public static final String general_success = "successful";
	public static final String addEvent_fail_cannot_decrease_capacity = "Cannot decrease capacity";
	public static final String addEvent_success_added = "Event added successfully";
	public static final String addEvent_success_capacity_updated = "Event updated successfully";
	public static final String removeEvent_fail_no_such_event = "No such event";
	public static final String bookEvent_fail_no_such_event = "No such event";
	public static final String bookEvent_fail_no_capacity = "Event is full";
	public static final String bookEvent_fail_weekly_limit = "Weekly limit reached";
	public static final String cancelEvent_fail_not_registered_in_event = "You are not registered in event";
	public static final String cancelEvent_fail_no_such_event = "No such event";
	public static final String swapEvent_fail_no_such_event = "No such event";
	public static final String swapEvent_fail_not_registered_in_event = "You are not registered in event";
	private static final String SUCCESS = "Success:";
	private static final String FAIL = "Fail:";

	private static String standardOutput(boolean isSuccess, String method, String output) {
		if (isSuccess)
			return SUCCESS + method + " > " + output;
		else
			return FAIL + method + " > " + output;
	}

	public static String addEventOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			if (reason == null) {
				reason = general_success;
			}
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "addEvent", reason);
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
		return standardOutput(isSuccess, "listEventAvailability", reason);
	}

	public static String removeEventOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "removeEvent", reason);
	}

	public static String bookEventOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "bookEvent", reason);
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
		return standardOutput(isSuccess, "getBookingSchedule", reason);
	}

	public static String cancelEventOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "cancelEvent", reason);
	}


	public static String swapEventOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "swapEvent", reason);
	}
}

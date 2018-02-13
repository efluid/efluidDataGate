package fr.uem.efluid.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@ControllerAdvice
public class ErrorController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ErrorController.class);

	/**
	 * Custom error display
	 * 
	 * @param e
	 * @param model
	 * @return
	 */
	@ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String exception(final Throwable e, final Model model) {

		LOGGER.error("Unprocessed exception found", e);

		model.addAttribute("error", e);

		// Code : for message display (can use payload)
		if (e instanceof ApplicationException) {
			ApplicationException ext = (ApplicationException) e;
			model.addAttribute("code", ext.getError());
			model.addAttribute("payload", ext.getPayload());
		} else {
			model.addAttribute("code", ErrorType.OTHER);
			model.addAttribute("payload", null);
		}

		// Code + timestamp : unique ID for error. Allows to find error in logs
		model.addAttribute("timestamp", Long.valueOf(System.currentTimeMillis()));

		return "error";
	}

}

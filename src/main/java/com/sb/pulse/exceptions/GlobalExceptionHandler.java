package com.sb.pulse.exceptions;


import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handler for UsernameAlreadyExistsException
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ModelAndView handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", ex.getMessage());
        mav.setViewName("/error");  // This will render the error.html page
        return mav;
    }

    // Handler for UsernameAlreadyExistsException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFoundException(ResourceNotFoundException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", ex.getMessage());
        mav.setViewName("/error");  // This will render the error.html page
        return mav;
    }

    // Handler for UsernameAlreadyExistsException
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ModelAndView handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", ex.getMessage());
        mav.setViewName("/error");  // This will render the error.html page
        return mav;
    }

    // Handler for other exceptions (optional)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        mav.setViewName("/error");
        return mav;
    }
}
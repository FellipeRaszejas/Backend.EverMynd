	package com.evermynd.common.exception;
	
	import java.time.Instant;
	import java.util.Map;
	
	import org.springframework.http.HttpStatus;
	import org.springframework.http.ResponseEntity;
	import org.springframework.web.bind.MethodArgumentNotValidException;
	import org.springframework.web.bind.annotation.ExceptionHandler;
	import org.springframework.web.bind.annotation.RestControllerAdvice;
	import com.evermynd.subscription.exception.PaymentFailedException;
	import com.evermynd.appointment.exception.AppointmentAccessDeniedException;
	import com.evermynd.appointment.exception.AppointmentConflictException;
	import com.evermynd.appointment.exception.AppointmentNotFoundException;
	import com.evermynd.doctor.exception.DoctorProfileNotFoundException;
	import com.evermynd.patient.exception.PatientProfileNotFoundException;
	import com.evermynd.subscription.exception.ActiveSubscriptionAlreadyExistsException;
	import com.evermynd.subscription.exception.SubscriptionNotFoundException;
	import com.evermynd.user.exception.EmailAlreadyInUseException;
	import com.evermynd.user.exception.InvalidCredentialsException;
	
	@RestControllerAdvice
	public class GlobalExceptionHandler {
	
	    @ExceptionHandler(EmailAlreadyInUseException.class)
	    public ResponseEntity<Object> handleConflict(RuntimeException ex) {
	        return build(HttpStatus.CONFLICT, ex.getMessage());
	    }
	
	    @ExceptionHandler(InvalidCredentialsException.class)
	    public ResponseEntity<Object> handleUnauthorized(RuntimeException ex) {
	        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
	    }
	
	    @ExceptionHandler({
	            AppointmentNotFoundException.class,
	            DoctorProfileNotFoundException.class,
	            PatientProfileNotFoundException.class,
	            SubscriptionNotFoundException.class
	    })
	    public ResponseEntity<Object> handleNotFound(RuntimeException ex) {
	        return build(HttpStatus.NOT_FOUND, ex.getMessage());
	    }
	
	    @ExceptionHandler({
	            AppointmentConflictException.class,
	            ActiveSubscriptionAlreadyExistsException.class
	    })
	    public ResponseEntity<Object> handleConflictState(RuntimeException ex) {
	        return build(HttpStatus.CONFLICT, ex.getMessage());
	    }
	
	    @ExceptionHandler(AppointmentAccessDeniedException.class)
	    public ResponseEntity<Object> handleForbidden(RuntimeException ex) {
	        return build(HttpStatus.FORBIDDEN, ex.getMessage());
	    }
	
	    @ExceptionHandler(MethodArgumentNotValidException.class)
	    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
	        String message = ex.getBindingResult().getFieldErrors().stream()
	                .findFirst()
	                .map(error -> error.getDefaultMessage())
	                .orElse("Dados inválidos");
	        return build(HttpStatus.BAD_REQUEST, message);
	    }
	
	    private ResponseEntity<Object> build(HttpStatus status, String message) {
	        return ResponseEntity.status(status).body(Map.of(
	                "timestamp", Instant.now().toString(),
	                "status", status.value(),
	                "message", message
	        ));
	    }
	    @ExceptionHandler(PaymentFailedException.class)
	    public ResponseEntity<Object> handlePaymentFailed(RuntimeException ex) {
	        return build(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
	    }
	}
package org.cardanofoundation.proofoforigin.api.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.cardanofoundation.proofoforigin.api.constants.UploadType;

/**
 * <p>
 * A Custom Annotation to setup upload SCM Data to ScanTrust.
 * </p>
 * 
 * <p>
 * <h4>Using Synchronous Upload API</h4>
 * </p>
 * 
 * @author (Sotatek) joey.dao
 * @category Common-annotation
 * @since 2023/07
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface UploadScmDataSync {

	/**
	 * <p>
	 * Indicate that the upload scm sync task is sync task or not.
	 * </p>
	 * <p>
	 * (That upload task result will affect to whole process or not)
	 * </p>
	 */
	public boolean doSync() default false;

	/**
	 * <p>
	 * Indicate that the upload type.
	 * </p>
	 */
	public UploadType uploadType() default UploadType.BOTTLE_SYNC;

	/**
	 * <p>
	 * The Custom Class type of the input of the annotated function.
	 * It could have many different class type.
	 * (It help to extract variable of some object arg of the function)
	 * </p>
	 */
	public Class<?>[] inputCustomClassTypes() default Object.class;
}

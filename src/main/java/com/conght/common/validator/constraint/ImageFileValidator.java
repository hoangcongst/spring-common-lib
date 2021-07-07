package com.conght.common.validator.constraint;

import com.conght.common.validator.annotation.ValidImage;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ImageFileValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    @Override
    public void initialize(ValidImage constraintAnnotation) {

    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        if(multipartFile == null)
            return true;
        boolean result = true;

        String contentType = multipartFile.getContentType();
        assert contentType != null;
        if (!isSupportedContentType(contentType)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Only PNG or JPG images are allowed.")
                    .addConstraintViolation();

            result = false;
        }

        return result;
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg");
    }
}
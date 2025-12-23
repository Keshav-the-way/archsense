import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export class CustomValidators {
  static fileSize(maxSizeInMB: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const file = control.value as File;
      if (!file) {
        return null;
      }

      const maxSizeInBytes = maxSizeInMB * 1024 * 1024;
      if (file.size > maxSizeInBytes) {
        return { fileSize: { max: maxSizeInMB, actual: file.size / 1024 / 1024 } };
      }

      return null;
    };
  }

  static fileType(allowedTypes: string[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const file = control.value as File;
      if (!file) {
        return null;
      }

      const fileType = file.type;
      if (!allowedTypes.includes(fileType)) {
        return { fileType: { allowed: allowedTypes, actual: fileType } };
      }

      return null;
    };
  }
}
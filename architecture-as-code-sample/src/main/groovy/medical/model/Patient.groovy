package medical.model

import base.Entity


class Patient extends Entity {

    enum Gender { male, female, unknown }

    String firstName
    String lastName
    int age
    Gender gender = Gender.unknown
    def address

    /*----------------------------------------------------------------------------------*/
    @Override
    String toString() {
        return "Patient - id: $id, firstName: $firstName, lastName: $lastName, gender: $gender"
    }
}

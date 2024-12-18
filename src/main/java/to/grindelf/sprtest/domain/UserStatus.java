package to.grindelf.sprtest.domain;

/**
 * Enum for User status.
 */
public enum UserStatus {

    /**
     * Admin status with all rights.
     * With great power comes great responsibility.
     */
    ADMIN {
        @Override
        public String toString() {
            return "admin";
        }
    },

    /**
     * Common user status - no admin rights.
     */
    JUST_USER {
        @Override
        public String toString() {
            return "just_user";
        }
    }
}

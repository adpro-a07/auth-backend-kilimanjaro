package id.ac.ui.cs.advprog.kilimanjaro.model.enums;

public enum UserRole {
    ADMIN,
    CUSTOMER,
    TECHNICIAN;

    public id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole toGrpcRole() {
        return switch (this) {
            case ADMIN -> id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.ADMIN;
            case CUSTOMER -> id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.CUSTOMER;
            case TECHNICIAN -> id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.TECHNICIAN;
        };
    }
}

package co.com.pragma.crediya.usecase.loan;

import co.com.pragma.crediya.model.loan.Type;
import co.com.pragma.crediya.model.loan.gateways.TypeRepository;
import reactor.core.publisher.Flux;

public class TypeUseCase {

    private final TypeRepository typeRepository;

    public TypeUseCase(TypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    public Flux<Type> getAllLoanTypes() {
        return typeRepository.findAll();
    }

}

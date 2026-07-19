package br.com.alexnunes.contaspagar.domain.importacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "importacao_erro")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportacaoErro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "importacao_id", nullable = false)
    private Importacao importacao;

    @Column(nullable = false)
    private int linha;

    @Column(nullable = false)
    private String mensagem;

    public ImportacaoErro(Importacao importacao, int linha, String mensagem) {
        this.importacao = importacao;
        this.linha = linha;
        this.mensagem = mensagem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImportacaoErro other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}

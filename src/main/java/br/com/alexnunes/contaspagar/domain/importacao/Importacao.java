package br.com.alexnunes.contaspagar.domain.importacao;

import br.com.alexnunes.contaspagar.domain.importacao.enums.ImportacaoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "importacao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Importacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String protocolo;

    @Column(name = "caminho_arquivo", nullable = false)
    private String caminhoArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportacaoStatus status;

    @Column(name = "total_registros", nullable = false)
    private int totalRegistros;

    @Column(nullable = false)
    private int sucesso;

    @Column(nullable = false)
    private int falhas;

    public Importacao(String protocolo, String caminhoArquivo) {
        this.protocolo = protocolo;
        this.caminhoArquivo = caminhoArquivo;
        this.status = ImportacaoStatus.PROCESSANDO;
        this.totalRegistros = 0;
        this.sucesso = 0;
        this.falhas = 0;
    }

    public void concluir(int totalRegistros, int sucesso, int falhas) {
        this.totalRegistros = totalRegistros;
        this.sucesso = sucesso;
        this.falhas = falhas;
        this.status = falhas > 0 ? ImportacaoStatus.CONCLUIDO_COM_ERROS : ImportacaoStatus.CONCLUIDO;
    }

    public void falhar() {
        this.status = ImportacaoStatus.FALHOU;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Importacao other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}

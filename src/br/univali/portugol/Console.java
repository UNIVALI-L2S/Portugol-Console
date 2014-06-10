package br.univali.portugol;

import br.univali.portugol.nucleo.ErroCompilacao;
import br.univali.portugol.nucleo.Portugol;
import br.univali.portugol.nucleo.Programa;
import br.univali.portugol.nucleo.analise.ResultadoAnalise;
import br.univali.portugol.nucleo.asa.TipoDado;
import br.univali.portugol.nucleo.execucao.ObservadorExecucao;
import br.univali.portugol.nucleo.execucao.ResultadoExecucao;
import br.univali.portugol.nucleo.execucao.es.Armazenador;
import br.univali.portugol.nucleo.execucao.es.Entrada;
import br.univali.portugol.nucleo.execucao.es.Saida;
import br.univali.portugol.nucleo.mensagens.AvisoAnalise;
import br.univali.portugol.nucleo.mensagens.ErroAnalise;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 *
 * @author Luiz Fernando Noschang
 */
public final class Console implements Entrada, Saida, ObservadorExecucao
{
    public static void main(String[] args)
    {args = new String[] { "D:\\Usuarios\\Luiz Fernando\\Desktop\\a.por" };
            
        try
        {
            Console console = new Console();
            console.executar(args);
        }
        catch (Exception excecao)
        {
            System.err.println(excecao.getMessage());
            System.err.flush();

            aguardar();
        }
    }

    private void executar(String[] args) throws Exception
    {
        File arquivo = extrairArquivo(args);
        args = extrairParametros(args);

        try
        {
            String algoritmo = lerArquivo(arquivo);

            Programa programa = Portugol.compilar(algoritmo);
            programa.setEntrada(this);
            programa.setSaida(this);
            programa.adicionarObservadorExecucao(this);

            if (programa.getResultadoAnalise().contemAvisos())
            {
                exibirResultadoAnalise(programa.getResultadoAnalise());
                System.out.println("\n\n");
                System.out.flush();
            }

            programa.setDiretorioTrabalho(arquivo.getParentFile());
            programa.executar(args);
        }
        catch (ErroCompilacao erro)
        {
            exibirResultadoAnalise(erro.getResultadoAnalise());
        }
    }

    private File extrairArquivo(String[] args) throws Exception
    {
        if (args.length > 0)
        {
            File arquivo = new File(args[0]);
            String caminho = obterCaminhoArquivo(arquivo);

            if (!arquivo.exists())
            {
                throw new Exception(String.format("O caminho '%s' não existe", caminho));
            }

            if (!arquivo.isFile())
            {
                throw new Exception(String.format("O caminho '%s' não é um arquivo", caminho));
            }

            if (!arquivo.getName().toLowerCase().endsWith(".por"))
            {
                throw new Exception(String.format("O arquivo '%s' não é um programa do Portugol", caminho));
            }

            if (!arquivo.canRead())
            {
                throw new Exception(String.format("O arquivo '%s' não pode ser lido", caminho));
            }

            return arquivo;
        }

        throw new Exception("O arquivo de programa não foi informado");
    }

    private String obterCaminhoArquivo(File arquivo)
    {
        try
        {
            return arquivo.getCanonicalPath();
        }
        catch (IOException ex)
        {
            return arquivo.getAbsolutePath();
        }
    }

    private String[] extrairParametros(String[] args)
    {
        if (args.length > 1)
        {
            String[] params = new String[args.length - 1];

            for (int i = 1; i < args.length; i++)
            {
                params[i - 1] = args[i];
            }

            return params;
        }

        return null;
    }

    @Override
    public void solicitaEntrada(TipoDado tipoDado, Armazenador armazenador) throws Exception
    {
        Scanner scanner = new Scanner(System.in);

        try
        {
            switch (tipoDado)
            {

                case CADEIA:
                    armazenador.setValor(scanner.next());
                    return;
                case CARACTER:
                    armazenador.setValor(scanner.next().charAt(0));
                    return;
                case INTEIRO:
                    armazenador.setValor(scanner.nextInt());
                    return;
                case REAL:
                    armazenador.setValor(scanner.nextDouble());
                    return;
                case LOGICO:
                {
                    String log = scanner.next();
                    Object valor = log.equals("verdadeiro") ? true : (log.equals("falso")) ? false : null;

                    armazenador.setValor(valor);
                    return;
                }
            }
        }
        catch (InputMismatchException excecao)
        {

        }

        armazenador.cancelarLeitura();
    }

    @Override
    public void limpar() throws Exception
    {

    }

    @Override
    public void escrever(String valor) throws Exception
    {
        System.out.print(valor);
        System.out.flush();
    }

    @Override
    public void escrever(boolean valor) throws Exception
    {
        if (valor == true)
        {
            System.out.print("verdadeiro");
            System.out.flush();
        }
        else
        {
            System.out.print("falso");
            System.out.flush();
        }
    }

    @Override
    public void escrever(int valor) throws Exception
    {
        System.out.print(Integer.toString(valor));
        System.out.flush();
    }

    @Override
    public void escrever(double valor) throws Exception
    {
        System.out.print(Double.toString(valor));
        System.out.flush();
    }

    @Override
    public void escrever(char valor) throws Exception
    {
        System.out.print(Character.toString(valor));
        System.out.flush();
    }

    @Override
    public void execucaoIniciada(Programa programa)
    {

    }

    @Override
    public void execucaoEncerrada(Programa programa, ResultadoExecucao resultadoExecucao)
    {
        switch (resultadoExecucao.getModoEncerramento())
        {
            case NORMAL:
                System.out.println("\nPrograma finalizado");
                System.out.flush();
                break;
            case INTERRUPCAO:
                System.out.println("\nO programa foi interrompido");
                System.out.flush();
                break;
            case ERRO:
                System.out.println("\nErro de execução: " + resultadoExecucao.getErro().getMensagem() + "\nLinha: " + resultadoExecucao.getErro().getLinha() + ", Coluna: " + resultadoExecucao.getErro().getColuna());
                System.out.flush();
                break;
        }

        aguardar();
    }

    private String lerArquivo(File arquivo) throws Exception
    {
        try
        {
            StringBuilder reading = new StringBuilder();
            FileInputStream is = new FileInputStream(arquivo);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
            String line;

            try
            {
                while ((line = reader.readLine()) != null)
                {
                    reading.append(line);
                    reading.append("\n");
                }

                return reading.toString().replace("/*${cursor}*/", "");
            }
            finally
            {
                try
                {
                    reader.close();
                }
                catch (IOException ex)
                {
                }
            }
        }
        catch (IOException ex)
        {
            throw new Exception(String.format("Ocorreu um erro ao ler o arquivo '%s'", obterCaminhoArquivo(arquivo)));
        }
    }

    private void exibirResultadoAnalise(ResultadoAnalise resultadoAnalise)
    {
        for (AvisoAnalise aviso : resultadoAnalise.getAvisos())
        {
            System.out.println("AVISO: " + aviso.getMensagem() + ". Linha: " + aviso.getLinha() + ", Coluna: " + aviso.getColuna());
            System.out.flush();
        }

        for (ErroAnalise erro : resultadoAnalise.getErros())
        {
            System.out.println("ERRO: " + erro.getMensagem() + ". Linha: " + erro.getLinha() + ", Coluna: " + erro.getColuna());
            System.out.flush();
        }
    }

    private static void aguardar()
    {
        try
        {
            System.out.println("");
            System.out.println("Pressione ENTER para continuar");
            System.out.flush();
            System.in.read();
            System.exit(0);
        }
        catch (IOException ex)
        {
            try
            {
                Thread.sleep(3000);
                System.exit(0);
            }
            catch (InterruptedException ex2)
            {

            }
        }
    }
}

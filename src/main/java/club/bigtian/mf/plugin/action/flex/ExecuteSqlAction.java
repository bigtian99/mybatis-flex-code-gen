package club.bigtian.mf.plugin.action.flex;

import com.intellij.database.actions.RunQueryAction;
import com.intellij.database.console.JdbcConsole;
import com.intellij.database.datagrid.DataRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecuteSqlAction extends RunQueryAction.Alt1 {

    public void actionPerformed(JdbcConsole console,String sql) {
        var request = new CreateRequest(console, sql, DataRequest.newConstraints(), null);
        console.getSession().getMessageBus().getDataProducer().processRequest(request);
    }


    public static class CreateRequest extends DataRequest.QueryRequest {
        protected CreateRequest(@NotNull Owner owner, @NotNull String query, @NotNull Constraints constraints, @Nullable Object params) {
            super(owner, query, constraints, params);
        }
    }
}
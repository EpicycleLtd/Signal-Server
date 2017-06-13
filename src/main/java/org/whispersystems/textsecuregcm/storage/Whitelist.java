package org.whispersystems.textsecuregcm.storage;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.whispersystems.textsecuregcm.entities.MessageProtos.Envelope;
import org.whispersystems.textsecuregcm.entities.OutgoingMessageEntity;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class Whitelist {

  private static final String ID     = "id";
  private static final String TYPE   = "type";
  private static final String NUMBER = "number";

  @Mapper(WhitelistMapper.class)
  @SqlQuery("SELECT * FROM whitelist WHERE " + NUMBER + " = :number AND " + TYPE + " = :type")
  abstract boolean isInWhitelist(@Bind("number") String number,
                                 @Bind("type")   long type
  );

  @SqlUpdate("VACUUM whitelist")
  public abstract void vacuum();

  public static class WhitelistMapper implements ResultSetMapper<Boolean> {
    @Override
    public Boolean map(int i, ResultSet resultSet, StatementContext statementContext)
        throws SQLException
    {
      return true;
    }
  }
}

package no.mnemonic.act.platform.service.ti.resolvers.request;

import no.mnemonic.act.platform.api.exceptions.InvalidArgumentException;
import no.mnemonic.act.platform.api.model.v1.Organization;
import no.mnemonic.act.platform.auth.OrganizationSPI;
import no.mnemonic.act.platform.dao.cassandra.FactManager;
import no.mnemonic.act.platform.dao.cassandra.ObjectManager;
import no.mnemonic.act.platform.dao.cassandra.OriginManager;
import no.mnemonic.act.platform.dao.cassandra.entity.FactTypeEntity;
import no.mnemonic.act.platform.dao.cassandra.entity.ObjectTypeEntity;
import no.mnemonic.act.platform.dao.cassandra.entity.OriginEntity;
import no.mnemonic.act.platform.service.contexts.SecurityContext;
import no.mnemonic.commons.utilities.collections.SetUtils;
import no.mnemonic.services.common.auth.InvalidCredentialsException;
import no.mnemonic.services.common.auth.model.Credentials;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.UUID;

import static no.mnemonic.commons.utilities.collections.SetUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class SearchByNameRequestResolverTest {

  @Mock
  private FactManager factManager;
  @Mock
  private ObjectManager objectManager;
  @Mock
  private OriginManager originManager;
  @Mock
  private OrganizationSPI organizationResolver;
  @Mock
  private SecurityContext securityContext;

  private SearchByNameRequestResolver resolver;

  @Before
  public void setUp() {
    initMocks(this);
    when(securityContext.getCredentials()).thenReturn(new Credentials() {});
    resolver = new SearchByNameRequestResolver(factManager, objectManager, originManager, organizationResolver, securityContext);
  }

  @Test
  public void testResolveWithNullInput() throws Exception {
    assertEquals(set(), resolver.resolveFactType(null));
    assertEquals(set(), resolver.resolveFactType(Collections.emptySet()));

    assertEquals(set(), resolver.resolveObjectType(null));
    assertEquals(set(), resolver.resolveObjectType(Collections.emptySet()));

    assertEquals(set(), resolver.resolveOrigin(null));
    assertEquals(set(), resolver.resolveOrigin(Collections.emptySet()));

    assertEquals(set(), resolver.resolveOrganization(null));
    assertEquals(set(), resolver.resolveOrganization(Collections.emptySet()));
  }

  @Test
  public void testResolveWithOnlyUUID() throws Exception {
    UUID id = UUID.randomUUID();

    assertEquals(set(id), resolver.resolveFactType(set(id.toString())));
    assertEquals(set(id), resolver.resolveObjectType(set(id.toString())));
    assertEquals(set(id), resolver.resolveOrigin(set(id.toString())));
    assertEquals(set(id), resolver.resolveOrganization(set(id.toString())));

    verifyNoMoreInteractions(factManager, objectManager, originManager, organizationResolver);
  }

  @Test
  public void testResolveWithOnlyNameNotFound() throws Exception {
    InvalidArgumentException ex = assertThrows(InvalidArgumentException.class, () -> resolver.resolveFactType(set("name1")));
    assertEquals(SetUtils.set("factType"), SetUtils.set(ex.getValidationErrors(), InvalidArgumentException.ValidationError::getProperty));

    InvalidArgumentException ex2 = assertThrows(InvalidArgumentException.class, () -> resolver.resolveObjectType(set("name2")));
    assertEquals(SetUtils.set("objectType"), SetUtils.set(ex2.getValidationErrors(), InvalidArgumentException.ValidationError::getProperty));

    InvalidArgumentException ex3 = assertThrows(InvalidArgumentException.class, () -> resolver.resolveOrigin(set("name3")));
    assertEquals(SetUtils.set("origin"), SetUtils.set(ex3.getValidationErrors(), InvalidArgumentException.ValidationError::getProperty));

    InvalidArgumentException ex4 = assertThrows(InvalidArgumentException.class, () -> resolver.resolveOrganization(set("name4")));
    assertEquals(SetUtils.set("organization"), SetUtils.set(ex4.getValidationErrors(), InvalidArgumentException.ValidationError::getProperty));

    verify(factManager).getFactType("name1");
    verify(objectManager).getObjectType("name2");
    verify(originManager).getOrigin("name3");
    verify(organizationResolver).resolveOrganization(notNull(), eq("name4"));
  }

  @Test
  public void testResolveWithOnlyNameFound() throws Exception {
    UUID id = UUID.randomUUID();
    when(factManager.getFactType("name1")).thenReturn(new FactTypeEntity().setId(id));
    when(objectManager.getObjectType("name2")).thenReturn(new ObjectTypeEntity().setId(id));
    when(originManager.getOrigin("name3")).thenReturn(new OriginEntity().setId(id));
    when(organizationResolver.resolveOrganization(notNull(), eq("name4"))).thenReturn(Organization.builder().setId(id).build());

    assertEquals(set(id), resolver.resolveFactType(set("name1")));
    assertEquals(set(id), resolver.resolveObjectType(set("name2")));
    assertEquals(set(id), resolver.resolveOrigin(set("name3")));
    assertEquals(set(id), resolver.resolveOrganization(set("name4")));

    verify(factManager).getFactType("name1");
    verify(objectManager).getObjectType("name2");
    verify(originManager).getOrigin("name3");
    verify(organizationResolver).resolveOrganization(notNull(), eq("name4"));
  }

  @Test
  public void testResolveWithBothNameAndUUID() throws Exception {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    when(factManager.getFactType("name")).thenReturn(new FactTypeEntity().setId(id2));
    when(objectManager.getObjectType("name")).thenReturn(new ObjectTypeEntity().setId(id2));
    when(originManager.getOrigin("name")).thenReturn(new OriginEntity().setId(id2));
    when(organizationResolver.resolveOrganization(notNull(), eq("name"))).thenReturn(Organization.builder().setId(id2).build());

    assertEquals(set(id1, id2), resolver.resolveFactType(set(id1.toString(), "name")));
    assertEquals(set(id1, id2), resolver.resolveObjectType(set(id1.toString(), "name")));
    assertEquals(set(id1, id2), resolver.resolveOrigin(set(id1.toString(), "name")));
    assertEquals(set(id1, id2), resolver.resolveOrganization(set(id1.toString(), "name")));
  }

  @Test
  public void testResolveOrganizationWithInvalidCredentials() throws Exception {
    when(organizationResolver.resolveOrganization(notNull(), eq("name"))).thenThrow(InvalidCredentialsException.class);

    InvalidArgumentException ex = assertThrows(InvalidArgumentException.class, () -> resolver.resolveOrganization(set("name")));
    assertEquals(SetUtils.set("organization"), SetUtils.set(ex.getValidationErrors(), InvalidArgumentException.ValidationError::getProperty));

    verify(organizationResolver).resolveOrganization(notNull(), eq("name"));
  }
}

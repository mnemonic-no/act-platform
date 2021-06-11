package no.mnemonic.act.platform.dao.facade.resolvers;

import no.mnemonic.act.platform.dao.api.record.ObjectRecord;
import no.mnemonic.act.platform.dao.cassandra.ObjectManager;
import no.mnemonic.act.platform.dao.cassandra.entity.ObjectEntity;
import no.mnemonic.act.platform.dao.facade.converters.ObjectRecordConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class GuavaBackedObjectResolverTest {

  @Mock
  private ObjectManager objectManager;
  @Mock
  private ObjectRecordConverter objectRecordConverter;

  private CachedObjectResolver objectResolver;

  @Before
  public void setUp() {
    initMocks(this);
    objectResolver = new GuavaBackedObjectResolver(objectManager, objectRecordConverter, new GuavaBackedObjectResolver.CacheConfiguration());
  }

  @Test
  public void testGetObjectByIdInvalidInput() {
    assertNull(objectResolver.getObject(null));
    verifyNoInteractions(objectManager);
  }

  @Test
  public void testGetObjectByIdNotFound() {
    UUID id = UUID.randomUUID();
    assertNull(objectResolver.getObject(id));
    verify(objectManager).getObject(id);
  }

  @Test
  public void testGetObjectByIdFound() {
    UUID id = UUID.randomUUID();
    when(objectManager.getObject(any())).thenReturn(new ObjectEntity());
    when(objectRecordConverter.fromEntity(any())).thenReturn(new ObjectRecord());

    assertNotNull(objectResolver.getObject(id));
    verify(objectManager).getObject(id);
    verify(objectRecordConverter).fromEntity(notNull());
  }

  @Test
  public void testGetObjectByIdFoundCached() {
    UUID id = UUID.randomUUID();
    when(objectManager.getObject(any())).thenReturn(new ObjectEntity());
    when(objectRecordConverter.fromEntity(any())).then(i -> new ObjectRecord());

    assertSame(objectResolver.getObject(id), objectResolver.getObject(id));
    verify(objectManager).getObject(id);
    verify(objectRecordConverter).fromEntity(notNull());
  }

  @Test
  public void testGetObjectByTypeValueInvalidInput() {
    assertNull(objectResolver.getObject(null, null));
    assertNull(objectResolver.getObject("type", null));
    assertNull(objectResolver.getObject(null, "value"));
    verifyNoInteractions(objectManager);
  }

  @Test
  public void testGetObjectByTypeValueNotFound() {
    String type = "type";
    String value = "value";
    assertNull(objectResolver.getObject(type, value));
    verify(objectManager).getObject(type, value);
  }

  @Test
  public void testGetObjectByTypeValueFound() {
    String type = "type";
    String value = "value";
    when(objectManager.getObject(any(), any())).thenReturn(new ObjectEntity());
    when(objectRecordConverter.fromEntity(any())).thenReturn(new ObjectRecord());

    assertNotNull(objectResolver.getObject(type, value));
    verify(objectManager).getObject(type, value);
    verify(objectRecordConverter).fromEntity(notNull());
  }

  @Test
  public void testGetObjectByTypeValueFoundCached() {
    String type = "type";
    String value = "value";
    when(objectManager.getObject(any(), any())).thenReturn(new ObjectEntity());
    when(objectRecordConverter.fromEntity(any())).then(i -> new ObjectRecord());

    assertSame(objectResolver.getObject(type, value), objectResolver.getObject(type, value));
    verify(objectManager).getObject(type, value);
    verify(objectRecordConverter).fromEntity(notNull());
  }
}

package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Entity;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import icy.plugin.PluginLoader;

/**
 * Represents the instance cache manager for generic cytomine entities. This class is not instantiable, use subclasses to get specific entity instances.
 * 
 * @author Daniel Felipe Gonzalez Obando
 * @param <K>
 *        The type of entity identifier.
 * @param <V>
 *        The type of entity stored.
 */
public abstract class EntityCache<K, V extends Entity>
{
    private static CacheManager cacheManager;
    protected static final long ENTITY_HEAP_SIZE = 500;

    static
    {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withClassLoader(PluginLoader.getLoader())
                .withDefaultSizeOfMaxObjectGraph(ENTITY_HEAP_SIZE)
                .build(true);
    }

    /**
     * Retrieves the cache manager.
     * 
     * @return The singleton instance of the cache manager.
     */
    public static CacheManager getCacheManager()
    {
        return cacheManager;
    }

    private CytomineClient client;
    private Cache<K, V> cache;

    /**
     * Creates an instance of the cache for this entity.
     * 
     * @param client
     *        The Cytomine client.
     */
    public EntityCache(CytomineClient client)
    {
        this.client = client;
    }

    /**
     * Retrieves the entity associated to the provided key. If the entity is not found, an {@link EntityCacheException} is thrown.
     * 
     * @param entityId
     *        The entity identifier.
     * @return The retrieved entity.
     * @throws EntityCacheException
     *         If the entity is not found in the cache.
     */
    public V retrieve(K entityId) throws EntityCacheException
    {
        V entity = getCache().get(entityId);
        if (entity == null)
        {
            throw new EntityCacheException(
                    String.format("Entity was not found in %s cache: %s", getValueClass().getSimpleName(),
                            entityId.toString()));
        }
        return entity;
    }

    /**
     * Stores the entity in the cache associating it to the provided identifier.
     * 
     * @param entityId
     *        Entity identifier.
     * @param entity
     *        The entity to be stored.
     */
    public void store(K entityId, V entity)
    {
        getCache().put(entityId, entity);
    }

    /**
     * @return Retrieves (creates if needed) the cache store associated to this entity.
     */
    public Cache<K, V> getCache()
    {
        if (cache == null)
        {
            cache = retrieveOrCreateCache();
        }
        return cache;
    }

    /**
     * Removes the entity associated to the provided identifier if it exist in the cache.
     * 
     * @param entityId
     *        Entity identifier.
     * @return true if the cache exist and the operation is successful. false otherwise.
     */
    public boolean remove(K entityId)
    {
        if (cache != null)
        {
            cache.remove(entityId);
            return true;
        }
        return false;
    }

    private Cache<K, V> retrieveOrCreateCache()
    {
        Cache<K, V> cache = cacheManager.getCache(getCacheAlias(), getKeyClass(), getValueClass());
        if (cache == null)
        {
            return cacheManager.createCache(getCacheAlias(),
                    CacheConfigurationBuilder
                            .newCacheConfigurationBuilder(getKeyClass(), getValueClass(),
                                    ResourcePoolsBuilder.heap(getHeapSize()))
                            .build());
        }
        return cache;
    }

    /**
     * @return This entity cache alias with the name of the class and the hash code of the client.
     */
    public String getCacheAlias()
    {
        return String.format("%s%d", getClass().getSimpleName(), getClient().hashCode());
    }

    /**
     * @return The Cytomine client.
     */
    protected CytomineClient getClient()
    {
        return client;
    }

    protected abstract Class<K> getKeyClass();

    protected abstract Class<V> getValueClass();

    /**
     * @return The heap size. Number of entities that can be stored in this cache.
     */
    public long getHeapSize()
    {
        return ENTITY_HEAP_SIZE;
    }

}

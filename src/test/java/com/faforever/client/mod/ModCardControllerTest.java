package com.faforever.client.mod;

import com.faforever.client.builders.ModBeanBuilder;
import com.faforever.client.builders.ModVersionBeanBuilder;
import com.faforever.client.domain.ModVersionBean;
import com.faforever.client.domain.ModVersionBean.ModType;
import com.faforever.client.fx.ImageViewHelper;
import com.faforever.client.i18n.I18n;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.test.UITest;
import com.faforever.client.vault.review.StarController;
import com.faforever.client.vault.review.StarsController;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ModCardControllerTest extends UITest {

  @Mock
  public ModService modService;
  @Mock
  public ImageViewHelper imageViewHelper;
  @Mock
  private NotificationService notificationService;
  @Mock
  private I18n i18n;
  @Mock
  private StarsController starsController;
  @Mock
  private StarController starController;

  @InjectMocks
  private ModCardController instance;
  private ModVersionBean modVersion;
  private ObservableList<ModVersionBean> installedMods;

  @BeforeEach
  public void setUp() throws Exception {
    installedMods = FXCollections.observableArrayList();
    doAnswer(invocation -> new SimpleObjectProperty<>(invocation.getArgument(0))).when(imageViewHelper)
        .createPlaceholderImageOnErrorObservable(any());

    when(starsController.valueProperty()).thenReturn(new SimpleFloatProperty());
    when(modService.getInstalledMods()).thenReturn(installedMods);
    when(i18n.get(ModType.UI.getI18nKey())).thenReturn(ModType.UI.name());

    modVersion = ModVersionBeanBuilder.create().defaultValues().mod(ModBeanBuilder.create().defaultValues().get()).get();

    when(modService.uninstallMod(any())).thenReturn(CompletableFuture.runAsync(() -> {
    }));
    when(modService.downloadAndInstallMod((ModVersionBean) any(), isNull(), isNull())).thenReturn(CompletableFuture.runAsync(() -> {
    }));

    loadFxml("theme/vault/mod/mod_card.fxml", clazz -> {
      if (clazz == StarsController.class) {
        return starsController;
      }
      if (clazz == StarController.class) {
        return starController;
      }
      return instance;
    });
  }

  @Test
  public void testSetMod() {
    when(modService.loadThumbnail(modVersion)).thenReturn(new Image("/theme/images/default_achievement.png"));
    instance.setEntity(modVersion);

    assertEquals(modVersion.getMod().getDisplayName(), instance.nameLabel.getText());
    assertEquals(modVersion.getMod().getAuthor(), instance.authorLabel.getText());
    assertNotNull(instance.thumbnailImageView.getImage());
    verify(modService).loadThumbnail(modVersion);
  }

  @Test
  public void testSetModNoThumbnail() {
    Image image = mock(Image.class);
    when(modService.loadThumbnail(modVersion)).thenReturn(image);

    instance.setEntity(modVersion);

    assertNotNull(instance.thumbnailImageView.getImage());
  }

  @Test
  public void testGetRoot() throws Exception {
    assertNull(instance.getRoot().getParent());
    assertEquals(instance.modTileRoot, instance.getRoot());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testShowModDetail() {
    Consumer<ModVersionBean> listener = mock(Consumer.class);
    instance.setOnOpenDetailListener(listener);
    instance.onShowModDetail();
    verify(listener).accept(any());
  }

  @Test
  public void testUiModLabel() {
    instance.setEntity(modVersion);
    assertEquals(ModType.UI.name(), instance.typeLabel.getText());
  }

  @Test
  public void installedButtonVisibility() {
    instance.setEntity(modVersion);

    when(modService.isInstalled(modVersion)).thenReturn(false);
    installedMods.add(modVersion);
    assertFalse(instance.uninstallButton.isVisible());
    assertTrue(instance.installButton.isVisible());

    when(modService.isInstalled(modVersion)).thenReturn(true);
    installedMods.remove(modVersion);
    assertTrue(instance.uninstallButton.isVisible());
    assertFalse(instance.installButton.isVisible());
  }
}
